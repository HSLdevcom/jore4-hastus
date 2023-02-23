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
class Route(
    private val identifier: String,
    private val description: String,
    private val serviceType: Int = 0,
    private val direction: Int = 0,
    private val serviceMode: Int
) : HastusData() {

    override val fieldName: String = "route"

    override fun getFields(): List<Any> {
        return listWithFieldName(identifier, description, serviceType, direction, serviceMode)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        if (identifier != other.identifier) return false
        if (description != other.description) return false
        if (serviceType != other.serviceType) return false
        if (direction != other.direction) return false
        if (serviceMode != other.serviceMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + serviceType.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + serviceMode.hashCode()
        return result
    }

    override fun toString(): String {
        return "Route(identifier='$identifier', description='$description', serviceType=$serviceType, direction=$direction, serviceMode=$serviceMode, fieldName='$fieldName')"
    }
}
