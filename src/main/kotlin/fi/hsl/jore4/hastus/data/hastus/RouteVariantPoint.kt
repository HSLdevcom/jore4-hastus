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
class RouteVariantPoint(
    private val place: String,
    specTpDistance: Double,
    private val isTimingPoint: Boolean,
    private val allowLoadTime: Boolean,
    private val regulatedTp: Boolean,
    private val stopLabel: String,
    private val routeIdAndVariantId: String
) : HastusData() {

    private val specTpDistance: NumberWithAccuracy

    init {
        // Transform distance in meters to a km.mmm formatted string
        this.specTpDistance = NumberWithAccuracy(specTpDistance / 1000.0, 0, 3)
    }

    override val fieldName = "rvpoint"

    override fun getFields(): List<Any> {
        return listWithFieldName(place, specTpDistance, isTimingPoint, allowLoadTime, regulatedTp, stopLabel, routeIdAndVariantId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteVariantPoint

        if (place != other.place) return false
        if (isTimingPoint != other.isTimingPoint) return false
        if (allowLoadTime != other.allowLoadTime) return false
        if (regulatedTp != other.regulatedTp) return false
        if (stopLabel != other.stopLabel) return false
        if (routeIdAndVariantId != other.routeIdAndVariantId) return false
        if (specTpDistance != other.specTpDistance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = place.hashCode()
        result = 31 * result + isTimingPoint.hashCode()
        result = 31 * result + allowLoadTime.hashCode()
        result = 31 * result + regulatedTp.hashCode()
        result = 31 * result + stopLabel.hashCode()
        result = 31 * result + routeIdAndVariantId.hashCode()
        result = 31 * result + specTpDistance.hashCode()
        return result
    }

    override fun toString(): String {
        return "RouteVariantPoint(place='$place', isTimingPoint=$isTimingPoint, allowLoadTime=$allowLoadTime, regulatedTp=$regulatedTp, stopLabel='$stopLabel', routeIdAndVariantId='$routeIdAndVariantId', specTpDistance=$specTpDistance, fieldName='$fieldName')"
    }
}
