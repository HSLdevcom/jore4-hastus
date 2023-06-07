package fi.hsl.jore4.hastus.data.hastus

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy

/**
 * A Route variant point Hastus element, represents a Jore4 scheduled stop point in journey pattern
 *
 * @property place Name of the Hastus place in Jore4
 * @property specTpDistance Accumulated distance from the previous timing point in meters.
 * Must be null, if [isTimingPoint] is false.
 * @property isTimingPoint Is the stop in use, 0 no, 1 yes
 * @property allowLoadTime Is it allowed to wait for other vehicle for loading, 0 no 1 yes
 * @property regulatedTp Is the stop a timing point in Jore4, 0 no 1 yes
 * @property stopLabel The stop label in Jore4
 * @property routeIdAndVariantId Combined line and stop label, no leading zeroes
 * @constructor Create empty Route variant point
 */
data class RouteVariantPoint(
    private val place: String?,
    private val specTpDistance: NumberWithAccuracy?,
    private val isTimingPoint: Boolean,
    private val allowLoadTime: Boolean,
    private val regulatedTp: Boolean,
    private val stopLabel: String,
    private val routeIdAndVariantId: String
) : HastusData() {

    init {
        require(isTimingPoint || specTpDistance == null) {
            "specTpDistance must be null when isTimingPoint=false"
        }
    }

    override fun getFields(): List<Any> {
        return listOf(
            place ?: "", // null to empty string
            specTpDistance ?: "", // empty string instead of decimal number if distance not given
            isTimingPoint,
            allowLoadTime,
            regulatedTp,
            stopLabel,
            routeIdAndVariantId
        )
    }
}
