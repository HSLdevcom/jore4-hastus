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
data class StopDistance(
    val stopStart: String,
    val stopEnd: String,
    private val baseInService: Boolean = true,
    val editedDistance: Int
) : HastusData() {

    override fun getFields(): List<Any> = listOf(
        stopStart,
        stopEnd,
        baseInService,
        editedDistance
    )
}
