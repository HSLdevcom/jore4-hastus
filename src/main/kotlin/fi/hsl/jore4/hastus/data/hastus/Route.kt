package fi.hsl.jore4.hastus.data.hastus

/**
 * A Route Hastus element, represents a Jore4 Line
 *
 * @property identifier Four letter identifier of the Jore4 line, XXXX
 * @property description Name of the line in Jore4
 * @property serviceType Constant 0
 * @property direction Constant 0
 * @property serviceMode 0 for bus and tram lines, 2 for trains
 * @constructor Create a Route with given values
 */
data class Route(
    private val identifier: String,
    private val description: String,
    private val serviceType: Int = 0,
    private val direction: Int = 0,
    private val serviceMode: Int
) : HastusData() {

    override fun getFields(): List<Any> = listOf(identifier, description, serviceType, direction, serviceMode)
}
