package fi.hsl.jore4.hastus.data.hastus

/**
 * Stop distance Hastus element. Distances between stops.
 *
 * @property stopStart Stop label from Jore4 fof the start
 * @property stopEnd Stop label from Jore4 fof the end
 * @property baseInService Constant 1
 * @property editedDistance The distance between the stops, no leading zeroes, format mmmm
 * @constructor Create empty Stop distance
 */
class StopDistance(
    val stopStart: String,
    val stopEnd: String,
    private val baseInService: Boolean = true,
    val editedDistance: Int
) : HastusData() {

    override val fieldName = "stpdist"

    override fun getFields(): List<Any> {
        return listWithFieldName(
            stopStart,
            stopEnd,
            baseInService,
            editedDistance
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StopDistance

        if (stopStart != other.stopStart) return false
        if (stopEnd != other.stopEnd) return false
        if (baseInService != other.baseInService) return false
        if (editedDistance != other.editedDistance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stopStart.hashCode()
        result = 31 * result + stopEnd.hashCode()
        result = 31 * result + baseInService.hashCode()
        result = 31 * result + editedDistance.hashCode()
        return result
    }

    override fun toString(): String {
        return "StopDistance(stopStart='$stopStart', stopEnd='$stopEnd', baseInService=$baseInService, editedDistance=$editedDistance, fieldName='$fieldName')"
    }
}
