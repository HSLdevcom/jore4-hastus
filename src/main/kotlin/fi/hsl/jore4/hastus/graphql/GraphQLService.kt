package fi.hsl.jore4.hastus.graphql

import com.expediagroup.graphql.client.jackson.types.OptionalInput
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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

/**
 * This service performs GraphQL queries and mutations to Hasura service. An instance of this class
 * is meant to be created separately for each HTTP(S) session, i.e. single request-response cycle.
 * The same HTTP headers are added to each GraphQL request.
 *
 * @param[client] Hasura client instance
 * @param[sessionHeaders] HTTP headers to add to GraphQL requests
 */
class GraphQLService(
    private val client: HasuraClient,
    private val sessionHeaders: Map<String, String>
) {

    private fun getDistancesBetweenRouteStopPoints(
        routeIds: List<UUID>,
        observationDate: LocalDate
    ): List<JoreDistanceBetweenTwoStopPoints> {
        val distancesQuery = DistanceBetweenStopPoints(
            variables = DistanceBetweenStopPoints.Variables(
                routes = OptionalInput.Defined(UUIDList(routeIds)),
                observation_date = observationDate
            )
        )

        val distancesResult: DistanceBetweenStopPoints.Result = client.sendRequest(distancesQuery, sessionHeaders)

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
     */
    fun deepFetchRoutes(
        uniqueRouteLabels: List<String>,
        priority: Int,
        observationDate: LocalDate
    ): FetchRoutesResult {
        val routesQuery = RoutesWithHastusData(
            variables = RoutesWithHastusData.Variables(
                route_labels = OptionalInput.Defined(uniqueRouteLabels),
                route_priority = priority,
                observation_date = observationDate
            )
        )

        val routesResult: RoutesWithHastusData.Result = client.sendRequest(routesQuery, sessionHeaders)

        val routesGQL: List<route_route> = routesResult.route_route

        val routeIds: List<UUID> = routesGQL.map { it.route_id }
        val distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints> = getDistancesBetweenRouteStopPoints(
            routeIds,
            observationDate
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
            .map { line ->
                val routesBelongingToLine: List<route_route> = routesGQL
                    .filter { r -> r.route_line?.label == line.label }

                ConversionsFromGraphQL.mapToJoreLineAndRoutes(
                    line,
                    routesBelongingToLine,
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
        validityPeriodStart: LocalDate,
        validityPeriodEnd: LocalDate
    ): Map<String, JoreJourneyPattern> {
        val journeyPatternsQuery = JourneyPatternsForRoutes(
            variables = JourneyPatternsForRoutes.Variables(
                route_labels = uniqueRouteLabels,
                validity_start = validityPeriodStart,
                validity_end = validityPeriodEnd
            )
        )

        val journeyPatternsResult: JourneyPatternsForRoutes.Result =
            client.sendRequest(journeyPatternsQuery, sessionHeaders)

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

    fun getVehicleTypes(): Map<Int, UUID> {
        val listVehicleTypesResult: ListVehicleTypes.Result = client.sendRequest(ListVehicleTypes(), sessionHeaders)

        return listVehicleTypesResult
            .timetables
            ?.timetables_vehicle_type_vehicle_type
            ?.associate {
                it.hsl_id.toInt() to it.vehicle_type_id
            }
            .orEmpty()
    }

    fun getDayTypes(): Map<String, UUID> {
        val listDayTypesResult: ListDayTypes.Result = client.sendRequest(ListDayTypes(), sessionHeaders)

        return listDayTypesResult
            .timetables
            ?.timetables_service_calendar_day_type
            ?.associate { it.label to it.day_type_id }
            .orEmpty()
    }

    fun persistVehicleScheduleFrame(
        journeyPatterns: Collection<JoreJourneyPattern>,
        vehicleScheduleFrame: JoreVehicleScheduleFrame
    ): UUID? {
        // Use synchronized block when persisting data via GraphQL, since there is no transaction
        // support for it. Multiple simultaneous modifications of the same table will fail.
        synchronized(SYNC_LOCK_OBJECT) {
            val journeyPatternRefMap = createJourneyPatternReferences(journeyPatterns)

            val insertVehicleScheduleFrame = InsertVehicleScheduleFrame(
                variables = InsertVehicleScheduleFrame.Variables(
                    vehicle_schedule_frame = ConversionsToGraphQL.mapToGraphQL(
                        vehicleScheduleFrame,
                        journeyPatternRefMap
                    )
                )
            )

            val insertVehicleScheduleFrameResult: InsertVehicleScheduleFrame.Result =
                client.sendRequest(insertVehicleScheduleFrame, sessionHeaders)

            return insertVehicleScheduleFrameResult
                .timetables
                ?.timetables_insert_vehicle_schedule_vehicle_schedule_frame_one
                ?.vehicle_schedule_frame_id
        }
    }

    fun createJourneyPatternReferences(
        journeyPatterns: Collection<JoreJourneyPattern>
    ): Map<UUID, JoreJourneyPatternReference> {
        val timestamp = OffsetDateTime.now()

        val insertJourneyPatternRefs = InsertJourneyPatternRefs(
            variables = InsertJourneyPatternRefs.Variables(
                journey_pattern_refs = journeyPatterns.map { jp ->
                    timetables_journey_pattern_journey_pattern_ref_insert_input(
                        journey_pattern_id = OptionalInput.Defined(jp.journeyPatternId),
                        observation_timestamp = OptionalInput.Defined(timestamp),
                        snapshot_timestamp = OptionalInput.Defined(timestamp),
                        type_of_line = OptionalInput.Defined(jp.typeOfLine),
                        scheduled_stop_point_in_journey_pattern_refs = OptionalInput.Defined(
                            timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_arr_rel_insert_input(
                                jp.stops.map(ConversionsToGraphQL::mapToGraphQL)
                            )
                        )
                    )
                }
            )
        )

        val insertJourneyPatternRefsResult: InsertJourneyPatternRefs.Result =
            client.sendRequest(insertJourneyPatternRefs, sessionHeaders)

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

    companion object {

        // used for synchronized block
        private val SYNC_LOCK_OBJECT = Object()
    }
}
