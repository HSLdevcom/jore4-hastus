package fi.hsl.jore4.hastus.data.hastus

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy

/**
 * A Route variant point Hastus element, represents a Jore4 scheduled stop point in journey pattern
 *
 * @property place Name of the hastus place in Jore4
 * @property specTpDistance Distance to the next stop in meters
 * @property isTimingPoint Is the stop in use, 0 no, 1 yes
 * @property allowLoadTime Is it allowed to wait for other vehicle for loading, 0 no 1 yes
 * @property regulatedTp Is the stop a timing point in Jore4, 0 no 1 yes
 * @property stopLabel The stop label in Jore4
 * @property routeIdAndVariantId Combined line and stop label, no leading zeroes
 * @constructor Create empty Route variant point
 */
data class RouteVariantPoint(
    private val place: String,
    private val specTpDistance: NumberWithAccuracy,
    private val isTimingPoint: Boolean,
    private val allowLoadTime: Boolean,
    private val regulatedTp: Boolean,
    private val stopLabel: String,
    private val routeIdAndVariantId: String
) : HastusData() {

    override fun getFields(): List<Any> {
        return listOf(place, specTpDistance, isTimingPoint, allowLoadTime, regulatedTp, stopLabel, routeIdAndVariantId)
    }

    override fun toString(): String {
        return "RouteVariantPoint(place='$place', isTimingPoint=$isTimingPoint, allowLoadTime=$allowLoadTime, regulatedTp=$regulatedTp, stopLabel='$stopLabel', routeIdAndVariantId='$routeIdAndVariantId', specTpDistance=$specTpDistance)"
    }
}
