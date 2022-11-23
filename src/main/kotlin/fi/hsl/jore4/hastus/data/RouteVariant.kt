package fi.hsl.jore4.hastus.data

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
class RouteVariant(
    private val identifier: String,
    private val description: String,
    private val direction: Int,
    private val reversible: Boolean = false,
    private val routeIdAndVariantId: String,
    private val routeId: String
) : HastusData() {

    override val fieldName = "rvariant"

    override fun getFields(): List<Any> {
        return listWithFieldName(identifier, description, direction, reversible, routeIdAndVariantId, routeId)
    }
}
