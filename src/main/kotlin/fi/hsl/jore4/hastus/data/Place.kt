package fi.hsl.jore4.hastus.data

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
}
