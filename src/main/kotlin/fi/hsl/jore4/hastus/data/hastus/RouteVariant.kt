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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteVariant

        if (identifier != other.identifier) return false
        if (description != other.description) return false
        if (direction != other.direction) return false
        if (reversible != other.reversible) return false
        if (routeIdAndVariantId != other.routeIdAndVariantId) return false
        if (routeId != other.routeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + reversible.hashCode()
        result = 31 * result + routeIdAndVariantId.hashCode()
        result = 31 * result + routeId.hashCode()
        return result
    }

    override fun toString(): String {
        return "RouteVariant(identifier='$identifier', description='$description', direction=$direction, reversible=$reversible, routeIdAndVariantId='$routeIdAndVariantId', routeId='$routeId', fieldName='$fieldName')"
    }
}
