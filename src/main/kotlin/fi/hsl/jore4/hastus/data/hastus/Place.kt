package fi.hsl.jore4.hastus.data.hastus

/**
 * Represents a Hastus Place element
 *
 * @property identifier Name of the Place
 * @property description Description of the Place
 * @constructor Creates a Hastus Place
 */
data class Place(
    private val identifier: String,
    private val description: String
) : HastusData() {

    override fun getFields(): List<Any> {
        return listOf(identifier, description)
    }

    override fun toString(): String {
        return "Place(identifier='$identifier', description='$description')"
    }
}
