package fi.hsl.jore4.hastus.data.hastus

/**
 * A Route variant Hastus element, represents a Jore4 route
 *
 * @property identifier Jore4 route label, combined with the direction of the route. Outbound 1, inbound 2
 * @property description Name of the route in Jore4
 * @property direction Direction as a number, 0 for outbound, 1 for inbound
 * @property reversible Constant 0
 * @property routeIdAndVariantId Combination of the line and variant ids together, format XXXXXX
 * @property routeId 5 first numbers of the line label, no leading zeroes
 * @constructor Create a Route variant with the given values
 */
data class RouteVariant(
    val identifier: String,
    val description: String,
    val direction: Int,
    val reversible: Boolean = false,
    val routeIdAndVariantId: String,
    val routeId: String
) : HastusData() {

    override fun getFields(): List<Any> =
        listOf(identifier, description, direction, reversible, routeIdAndVariantId, routeId)
}
