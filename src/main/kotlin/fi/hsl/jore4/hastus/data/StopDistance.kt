package fi.hsl.jore4.hastus.data

/**
 * Stop distance Hastus element. Distances between stops. TODO
 *
 * @property stopStart Stop label from Jore4 fof the start
 * @property stopEnd Stop label from Jore4 fof the end
 * @property baseInService Constant 1
 * @property editedDistance The distance between the stops, no leading zeroes, format mmmm
 * @constructor Create empty Stop distance
 */
class StopDistance(
    private val stopStart: String,
    private val stopEnd: String,
    private val baseInService: Boolean = true,
    private val editedDistance: Int
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
}
