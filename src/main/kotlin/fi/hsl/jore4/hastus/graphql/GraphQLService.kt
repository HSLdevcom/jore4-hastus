package fi.hsl.jore4.hastus.graphql

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.jackson.types.OptionalInput
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.StopDistance
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternReference
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreStopPoint
import fi.hsl.jore4.hastus.data.jore.JoreStopReference
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.data.mapper.GraphQLConverter
import fi.hsl.jore4.hastus.data.mapper.HastusConverter
import fi.hsl.jore4.hastus.generated.DistanceBetweenStopPoints
import fi.hsl.jore4.hastus.generated.InsertJourneyPatternRefs
import fi.hsl.jore4.hastus.generated.InsertVehicleScheduleFrame
import fi.hsl.jore4.hastus.generated.JourneyPatternsForRoutes
import fi.hsl.jore4.hastus.generated.ListDayTypes
import fi.hsl.jore4.hastus.generated.ListVehicleTypes
import fi.hsl.jore4.hastus.generated.RoutesWithHastusData
import fi.hsl.jore4.hastus.generated.inputs.timetables_journey_pattern_journey_pattern_ref_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_arr_rel_insert_input
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_line
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point
import fi.hsl.jore4.hastus.graphql.converter.ResultConverter
import fi.hsl.jore4.hastus.util.CsvWriter
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

private val LOGGER = KotlinLogging.logger {}

@Service
class GraphQLService(
    config: HasuraConfiguration,
    val objectMapper: ObjectMapper
) {

    private val client = GraphQLKtorClient(
        url = URL(config.url),
        httpClient = HttpClient {
            defaultRequest {
                contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
            }
            install(Logging) {
                // Can be changed to HEADERS for debugging purposes
                level = LogLevel.INFO
            }
        },
        serializer = GraphQLClientJacksonSerializer()
    )

    private val writer = CsvWriter()

    private fun <T : Any> sendRequest(
        request: GraphQLClientRequest<T>,
        headers: Map<String, String>
    ): T? {
        return runBlocking {
            LOGGER.debug {
                "GraphQL request:\n${request.query},\nvariables: ${
                    objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(request.variables)
                }"
            }

            val queryResponse: GraphQLClientResponse<T> = client.execute(request) {
                headers.map { header(it.key, it.value) }
            }

            LOGGER.debug {
                "GraphQL ${request.operationName} response: ${
                    objectMapper
                        // .writerWithDefaultPrettyPrinter() // disable commenting to get pretty-printed output
                        .writeValueAsString(queryResponse)
                }"
            }

            if (queryResponse.errors?.isNotEmpty() == true) {
                throw IllegalStateException(queryResponse.errors?.toString())
            }

            queryResponse.data
        }
    }

    private fun convertRoutes(
        routes: List<route_route>,
        distances: Map<Pair<String, String>, Int>
    ): List<IHastusData> {
        val dbLines: List<route_line> = routes.mapNotNull { it.route_line }.distinctBy { it.label }
        val joreLines: List<JoreLine> = dbLines.map {
            ResultConverter.mapJoreLine(
                it,
                routes.filter { r -> r.route_line?.label == it.label },
                distances
            )
        }

        val stops: List<service_pattern_scheduled_stop_point> = routes
            .flatMap { it.route_journey_patterns }
            .flatMap { it.scheduled_stop_point_in_journey_patterns }
            .flatMap { it.scheduled_stop_points }
            .distinct()

        val joreStops: List<JoreScheduledStop> = stops.map { ResultConverter.mapJoreStop(it) }
        val joreTimingPlaces: List<JoreTimingPlace> = stops
            .mapNotNull { it.timing_place }
            .distinct()
            .map { ResultConverter.mapJoreTimingPlace(it) }

        return HastusConverter.convertJoreLinesToHastus(joreLines) +
            HastusConverter.convertJoreStopsToHastus(joreStops) +
            HastusConverter.convertJoreTimingPlacesToHastus(joreTimingPlaces)
    }

    private fun convertDistances(distances: List<JoreDistanceBetweenTwoStopPoints>): List<StopDistance> {
        return distances.map {
            StopDistance(
                stopStart = it.startLabel,
                stopEnd = it.endLabel,
                editedDistance = it.distance
            )
        }
    }

    private fun getStopDistances(
        routeIds: List<UUID>,
        observationDate: LocalDate,
        headers: Map<String, String>
    ): List<JoreDistanceBetweenTwoStopPoints> {
        val distancesQuery = DistanceBetweenStopPoints(
            variables = DistanceBetweenStopPoints.Variables(
                routes = OptionalInput.Defined(UUIDList(routeIds)),
                observation_date = observationDate
            )
        )

        val distancesBetweenStops: DistanceBetweenStopPoints.Result? = sendRequest(distancesQuery, headers)

        val transformedDistances = distancesBetweenStops
            ?.service_pattern_get_distances_between_stop_points_by_routes
            ?.map(ResultConverter::mapJoreDistance)
            .orEmpty()

        return transformedDistances.distinct()
    }

    fun getRoutes(
        uniqueRoutes: List<String>,
        priority: Int,
        observationDate: LocalDate,
        headers: Map<String, String>
    ): String {
        val routesQuery = RoutesWithHastusData(
            variables = RoutesWithHastusData.Variables(
                route_labels = OptionalInput.Defined(uniqueRoutes),
                route_priority = priority,
                observation_date = observationDate
            )
        )

        val routes: RoutesWithHastusData.Result? = sendRequest(routesQuery, headers)

        val routeIds: List<UUID> = routes?.route_route?.map { it.route_id }.orEmpty()
        val distancesBetweenStops: List<JoreDistanceBetweenTwoStopPoints> = getStopDistances(
            routeIds,
            observationDate,
            headers
        )

        val distanceMap: Map<Pair<String, String>, Int> = distancesBetweenStops.associate {
            (it.startLabel to it.endLabel) to it.distance
        }

        val routesAndVariants: List<IHastusData> = convertRoutes(routes?.route_route.orEmpty(), distanceMap)
        val transformedDistances: List<StopDistance> = convertDistances(distancesBetweenStops)

        return (routesAndVariants + transformedDistances).distinct()
            .joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }

    fun getJourneyPatternsForRoutes(
        uniqueRoutes: List<String>,
        headers: Map<String, String>
    ): Map<String, JoreJourneyPattern> {
        val journeyPatternsQuery = JourneyPatternsForRoutes(
            variables = JourneyPatternsForRoutes.Variables(
                route_labels = OptionalInput.Defined(uniqueRoutes)
            )
        )

        val journeyPatterns: JourneyPatternsForRoutes.Result? = sendRequest(journeyPatternsQuery, headers)

        return journeyPatterns
            ?.route_route
            ?.associate {
                it.unique_label.orEmpty() to JoreJourneyPattern(
                    it.unique_label,
                    it.route_journey_patterns[0].journey_pattern_id,
                    it.route_line?.type_of_line.toString().lowercase(),
                    it.route_journey_patterns[0].scheduled_stop_point_in_journey_patterns.map { stop ->
                        JoreStopPoint(
                            stop.scheduled_stop_points.first().scheduled_stop_point_id,
                            stop.scheduled_stop_point_label,
                            stop.scheduled_stop_point_sequence + 1
                        )
                    }
                )
            }
            .orEmpty()
    }

    fun getVehicleTypes(
        headers: Map<String, String>
    ): Map<Int, UUID> {
        val vehicleTypes: ListVehicleTypes.Result? = sendRequest(ListVehicleTypes(), headers)

        return vehicleTypes
            ?.timetables
            ?.timetables_vehicle_type_vehicle_type
            ?.associate {
                it.hsl_id.toInt() to it.vehicle_type_id
            }
            .orEmpty()
    }

    fun getDayTypes(
        headers: Map<String, String>
    ): Map<String, UUID> {
        val dayTypes: ListDayTypes.Result? = sendRequest(ListDayTypes(), headers)

        return dayTypes
            ?.timetables
            ?.timetables_service_calendar_day_type
            ?.associate { it.label to it.day_type_id }
            .orEmpty()
    }

    fun persistVehicleScheduleFrame(
        journeyPatterns: Collection<JoreJourneyPattern>,
        vehicleScheduleFrame: JoreVehicleScheduleFrame,
        headers: Map<String, String>
    ): String {
        val journeyPatternRefMap = createJourneyPatternReferences(
            journeyPatterns,
            headers
        )

        val insertVehicleScheduleFrames = InsertVehicleScheduleFrame(
            variables = InsertVehicleScheduleFrame.Variables(
                vehicle_schedule_frame = GraphQLConverter.mapToGraphQL(vehicleScheduleFrame, journeyPatternRefMap)
            )
        )

        val vehicleScheduleFrames: InsertVehicleScheduleFrame.Result? = sendRequest(insertVehicleScheduleFrames, headers)

        return vehicleScheduleFrames
            ?.timetables
            ?.timetables_insert_vehicle_schedule_vehicle_schedule_frame_one
            ?.vehicle_schedule_frame_id.toString()
    }

    fun createJourneyPatternReferences(
        journeyPatterns: Collection<JoreJourneyPattern>,
        headers: Map<String, String>
    ): Map<UUID, JoreJourneyPatternReference> {
        val timestamp = OffsetDateTime.now()

        val insertJourneyPatternRefs = InsertJourneyPatternRefs(
            variables = InsertJourneyPatternRefs.Variables(
                journey_pattern_refs = journeyPatterns.map {
                    timetables_journey_pattern_journey_pattern_ref_insert_input(
                        journey_pattern_id = OptionalInput.Defined(it.journeyPatternId),
                        observation_timestamp = OptionalInput.Defined(timestamp),
                        snapshot_timestamp = OptionalInput.Defined(timestamp),
                        type_of_line = OptionalInput.Defined(it.typeOfLine),
                        scheduled_stop_point_in_journey_pattern_refs = OptionalInput.Defined(
                            timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_arr_rel_insert_input(
                                it.stops.map { stop -> GraphQLConverter.mapToGraphQL(stop) }
                            )
                        )
                    )
                }
            )
        )

        val journeyPatternRefs: InsertJourneyPatternRefs.Result? = sendRequest(insertJourneyPatternRefs, headers)

        return journeyPatternRefs
            ?.timetables
            ?.timetables_insert_journey_pattern_journey_pattern_ref
            ?.returning
            ?.associate {
                it.journey_pattern_id to JoreJourneyPatternReference(
                    it.journey_pattern_ref_id,
                    it.journey_pattern_id,
                    it.scheduled_stop_point_in_journey_pattern_refs.map { stop ->
                        JoreStopReference(
                            stop.scheduled_stop_point_in_journey_pattern_ref_id,
                            stop.scheduled_stop_point_label,
                            stop.scheduled_stop_point_sequence
                        )
                    }
                )
            }
            .orEmpty()
    }
}
