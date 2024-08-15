package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import fi.hsl.jore4.hastus.generated.insertjourneypatternrefs.timetables_journey_pattern_journey_pattern_ref as insert_journey_pattern_ref
import fi.hsl.jore4.hastus.generated.journeypatternrefs.timetables_journey_pattern_journey_pattern_ref as get_journey_pattern_ref

/**
 * Jore journey pattern reference
 *
 * @property[journeyPatternRefId] The UUID of the journey pattern reference
 * @property[journeyPatternId] The UUID of the original journey pattern
 * @property[routeUniqueLabel] The unique label (route label + variant) of the route associated with
 * the journey pattern
 * @property[routeDirection] The direction of the route associated with the journey pattern
 * @property[routeValidityStart] The date when the route becomes valid. If NULL, the route has been
 * always valid before the end date of the validity period.
 * @property[routeValidityEnd] The date from which onwards the route is no longer valid. If NULL,
 * the route is valid indefinitely after the start date of the validity period.
 * @property[typeOfLine] Type of the line the journey pattern is associated with, for example
 * "regional_bus_service"
 * @property[snapshotTime] The instant when the journey pattern reference was created
 * @property[observationTime] The instant when the route referenced by the original journey pattern
 * was valid
 * @property[stops] The list of references to stop points in the original journey pattern
 */
data class JoreJourneyPatternRef(
    val journeyPatternRefId: UUID,
    val journeyPatternId: UUID,
    val routeUniqueLabel: String,
    val routeDirection: JoreRouteDirection,
    val routeValidityStart: LocalDate?,
    val routeValidityEnd: LocalDate?,
    val typeOfLine: String,
    val snapshotTime: OffsetDateTime,
    val observationTime: OffsetDateTime,
    val stops: List<JoreJourneyPatternStopRef>
) {
    val routeLabelAndDirection
        get() = RouteLabelAndDirection(routeUniqueLabel, routeDirection)

    companion object {
        fun from(gql: get_journey_pattern_ref) =
            JoreJourneyPatternRef(
                journeyPatternRefId = gql.journey_pattern_ref_id,
                journeyPatternId = gql.journey_pattern_id,
                routeUniqueLabel = gql.route_label,
                routeDirection = JoreRouteDirection.from(gql.route_direction),
                routeValidityStart = gql.route_validity_start,
                routeValidityEnd = gql.route_validity_end,
                typeOfLine = gql.type_of_line,
                snapshotTime = gql.snapshot_timestamp,
                observationTime = gql.observation_timestamp,
                stops =
                    gql.scheduled_stop_point_in_journey_pattern_refs.map { stop ->
                        JoreJourneyPatternStopRef(
                            id = stop.scheduled_stop_point_in_journey_pattern_ref_id,
                            stopSequenceNumber = stop.scheduled_stop_point_sequence,
                            stopLabel = stop.scheduled_stop_point_label,
                            timingPlaceCode = stop.timing_place_label
                        )
                    }
            )

        fun from(gql: insert_journey_pattern_ref) =
            JoreJourneyPatternRef(
                journeyPatternRefId = gql.journey_pattern_ref_id,
                journeyPatternId = gql.journey_pattern_id,
                routeUniqueLabel = gql.route_label,
                routeDirection = JoreRouteDirection.from(gql.route_direction),
                routeValidityStart = gql.route_validity_start,
                routeValidityEnd = gql.route_validity_end,
                typeOfLine = gql.type_of_line,
                snapshotTime = gql.snapshot_timestamp,
                observationTime = gql.observation_timestamp,
                stops =
                    gql.scheduled_stop_point_in_journey_pattern_refs.map { stop ->
                        JoreJourneyPatternStopRef(
                            id = stop.scheduled_stop_point_in_journey_pattern_ref_id,
                            stopSequenceNumber = stop.scheduled_stop_point_sequence,
                            stopLabel = stop.scheduled_stop_point_label,
                            timingPlaceCode = stop.timing_place_label
                        )
                    }
            )
    }
}
