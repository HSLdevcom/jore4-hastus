package fi.hsl.jore4.hastus.graphql

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.jackson.types.OptionalInput
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternReference
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreStopPoint
import fi.hsl.jore4.hastus.data.jore.JoreStopReference
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.generated.DistanceBetweenStopPoints
import fi.hsl.jore4.hastus.generated.InsertJourneyPatternRefs
import fi.hsl.jore4.hastus.generated.InsertVehicleScheduleFrame
import fi.hsl.jore4.hastus.generated.JourneyPatternsForRoutes
import fi.hsl.jore4.hastus.generated.ListDayTypes
import fi.hsl.jore4.hastus.generated.ListVehicleTypes
import fi.hsl.jore4.hastus.generated.RoutesWithHastusData
import fi.hsl.jore4.hastus.generated.inputs.timetables_journey_pattern_journey_pattern_ref_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_arr_rel_insert_input
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point
import fi.hsl.jore4.hastus.graphql.converter.ConversionsFromGraphQL
import fi.hsl.jore4.hastus.graphql.converter.ConversionsToGraphQL
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

    private fun <T : Any> sendRequest(
        request: GraphQLClientRequest<T>,
        headers: Map<String, String>
    ): T {
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

            queryResponse.errors?.let { errorList ->
                if (errorList.isNotEmpty()) {
                    throw IllegalStateException(errorList.toString())
                }
            }

            queryResponse.data
                ?: throw IllegalStateException("GraphQL response did not contain data even when no errors were present")
        }
    }

    fun getStopDistances(
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

        val distancesResult: DistanceBetweenStopPoints.Result = sendRequest(distancesQuery, headers)

        val distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints> = distancesResult
            .service_pattern_get_distances_between_stop_points_by_routes
            .map(ConversionsFromGraphQL::mapToJoreDistance)

        return distancesBetweenStopPoints.distinct()
    }

    /**
     * Fetch routes via Jore4 GraphQL API. The parameters are used to constrain the set of routes to
     * be fetched. Returns a deep object hierarchy for routes used in Hastus CSV export.
     *
     * @param [uniqueRouteLabels] The labels of the routes to fetch
     * @param [priority] The priority used to constrain the routes to be fetched
     * @param [observationDate] The date used to filter active/valid routes
     * @param [headers] HTTP headers from the request to be used while querying GraphQL API
     */
    fun deepFetchRoutes(
        uniqueRouteLabels: List<String>,
        priority: Int,
        observationDate: LocalDate,
        headers: Map<String, String>
    ): FetchRoutesResult {
        val routesQuery = RoutesWithHastusData(
            variables = RoutesWithHastusData.Variables(
                route_labels = OptionalInput.Defined(uniqueRouteLabels),
                route_priority = priority,
                observation_date = observationDate
            )
        )

        val routesResult: RoutesWithHastusData.Result = sendRequest(routesQuery, headers)

        val routesGQL: List<route_route> = routesResult.route_route

        val routeIds: List<UUID> = routesGQL.map { it.route_id }
        val distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints> = getStopDistances(
            routeIds,
            observationDate,
            headers
        )

        val lines: List<JoreLine> = convertLinesAndRoutes(routesGQL, distancesBetweenStopPoints)

        val (stopPoints, timingPlaces) = extractStopPointsAndTimingPlaces(routesGQL)

        return FetchRoutesResult(lines, stopPoints, timingPlaces, distancesBetweenStopPoints)
    }

    private fun convertLinesAndRoutes(
        routesGQL: List<route_route>,
        distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints>
    ): List<JoreLine> {
        val distancesIndexedByStopLabels: Map<Pair<String, String>, Double> = distancesBetweenStopPoints.associate {
            (it.startLabel to it.endLabel) to it.distance
        }

        return routesGQL
            .mapNotNull { it.route_line }
            .distinctBy { it.label } // line label
            .map {
                ConversionsFromGraphQL.mapToJoreLineAndRoutes(
                    it,
                    routesGQL.filter { r -> r.route_line?.label == it.label },
                    distancesIndexedByStopLabels
                )
            }
    }

    private fun extractStopPointsAndTimingPlaces(routesGQL: List<route_route>): Pair<List<JoreScheduledStop>, List<JoreTimingPlace>> {
        val stopPointsGQL: List<service_pattern_scheduled_stop_point> = routesGQL
            .flatMap { it.route_journey_patterns }
            .flatMap { it.scheduled_stop_point_in_journey_patterns }
            .flatMap { it.scheduled_stop_points }
            .distinct()

        val stopPoints: List<JoreScheduledStop> = stopPointsGQL.map(ConversionsFromGraphQL::mapToJoreStop)

        val timingPlaces: List<JoreTimingPlace> = stopPointsGQL
            .mapNotNull { it.timing_place }
            .distinct()
            .map(ConversionsFromGraphQL::mapToJoreTimingPlace)

        return stopPoints to timingPlaces
    }

    fun getJourneyPatternsIndexingByRouteLabel(
        uniqueRouteLabels: List<String>,
        headers: Map<String, String>
    ): Map<String, JoreJourneyPattern> {
        val journeyPatternsQuery = JourneyPatternsForRoutes(
            variables = JourneyPatternsForRoutes.Variables(
                route_labels = OptionalInput.Defined(uniqueRouteLabels)
            )
        )

        val journeyPatternsResult: JourneyPatternsForRoutes.Result = sendRequest(journeyPatternsQuery, headers)

        return journeyPatternsResult
            .route_route
            .associate {
                it.unique_label.orEmpty() to JoreJourneyPattern(
                    it.route_journey_patterns[0].journey_pattern_id,
                    it.unique_label,
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
    }

    fun getVehicleTypes(
        headers: Map<String, String>
    ): Map<Int, UUID> {
        val listVehicleTypesResult: ListVehicleTypes.Result = sendRequest(ListVehicleTypes(), headers)

        return listVehicleTypesResult
            .timetables
            ?.timetables_vehicle_type_vehicle_type
            ?.associate {
                it.hsl_id.toInt() to it.vehicle_type_id
            }
            .orEmpty()
    }

    fun getDayTypes(
        headers: Map<String, String>
    ): Map<String, UUID> {
        val listDayTypesResult: ListDayTypes.Result = sendRequest(ListDayTypes(), headers)

        return listDayTypesResult
            .timetables
            ?.timetables_service_calendar_day_type
            ?.associate { it.label to it.day_type_id }
            .orEmpty()
    }

    /* Use synchronized when persisting data to graphQL, since there is no transaction support for it.
    *  Multiple simultaneous modifications of the same table will fail.
    */
    @Synchronized
    fun persistVehicleScheduleFrame(
        journeyPatterns: Collection<JoreJourneyPattern>,
        vehicleScheduleFrame: JoreVehicleScheduleFrame,
        headers: Map<String, String>
    ): UUID? {
        val journeyPatternRefMap = createJourneyPatternReferences(
            journeyPatterns,
            headers
        )

        val insertVehicleScheduleFrame = InsertVehicleScheduleFrame(
            variables = InsertVehicleScheduleFrame.Variables(
                vehicle_schedule_frame = ConversionsToGraphQL.mapToGraphQL(vehicleScheduleFrame, journeyPatternRefMap)
            )
        )

        val insertVehicleScheduleFrameResult: InsertVehicleScheduleFrame.Result =
            sendRequest(insertVehicleScheduleFrame, headers)

        return insertVehicleScheduleFrameResult
            .timetables
            ?.timetables_insert_vehicle_schedule_vehicle_schedule_frame_one
            ?.vehicle_schedule_frame_id
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
                                it.stops.map(ConversionsToGraphQL::mapToGraphQL)
                            )
                        )
                    )
                }
            )
        )

        val insertJourneyPatternRefsResult: InsertJourneyPatternRefs.Result =
            sendRequest(insertJourneyPatternRefs, headers)

        return insertJourneyPatternRefsResult
            .timetables
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
