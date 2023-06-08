package fi.hsl.jore4.hastus.data.jore

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
)
