package fi.hsl.jore4.hastus.data.hastus.exp

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
    val identifier: String,
    val description: String,
    val serviceType: Int = 0,
    val direction: Int = 0,
    val serviceMode: Int
) : IExportableItem {
    override fun getFields(): List<Any> = listOf(identifier, description, serviceType, direction, serviceMode)
}
