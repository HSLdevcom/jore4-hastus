package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.generated.distancebetweenstoppoints.service_pattern_distance_between_stops_calculation

/**
 * Jore distance between two stop points
 *
 * @property startLabel Label of the stop point from where distance is measured
 * @property endLabel Label of the stop point where the distance is measured to
 * @property distance Distance in meters
 */
data class JoreDistanceBetweenTwoStopPoints(
    val startLabel: String,
    val endLabel: String,
    val distance: Double
) {
    companion object {
        fun from(distance: service_pattern_distance_between_stops_calculation) =
            JoreDistanceBetweenTwoStopPoints(
                distance.start_stop_label,
                distance.end_stop_label,
                distance.distance_in_metres.toDouble() // transform decimal number from String format to Double
            )
    }
}
