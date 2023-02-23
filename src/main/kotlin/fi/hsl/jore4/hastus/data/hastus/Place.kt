package fi.hsl.jore4.hastus.data.hastus

/**
 * Represents a Hastus Place element
 *
 * @property identifier Name of the Place
 * @property description Description of the Place
 * @constructor Creates a Hastus Place
 */
class Place(
    private val identifier: String,
    private val description: String
) : HastusData() {

    override val fieldName = "place"

    override fun getFields(): List<Any> {
        return listWithFieldName(identifier, description)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Place

        if (identifier != other.identifier) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    override fun toString(): String {
        return "Place(identifier='$identifier', description='$description', fieldName='$fieldName')"
    }
}
