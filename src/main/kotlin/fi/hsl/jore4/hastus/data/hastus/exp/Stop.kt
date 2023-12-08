package fi.hsl.jore4.hastus.data.hastus.exp

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy

/**
 * Hastus Stop point representing a Jore4 scheduled stop point
 *
 * @property identifier Label of the stop point
 * @property platform Platform number of the stop point. Formatted as two numbers NN.
 * @property descriptionFinnish Name of the stop point in Finnish
 * @property descriptionSwedish Name of the stop point in Swedish
 * @property streetFinnish Street the stop point is located on in Finnish
 * @property streetSwedish Street the stop point is located on in Swedish
 * @property place Name of the location specified in Jore4, if there is one. Format as XXXXXX
 * @property latitude Latitude (ordinate) of the stop point. Formatted as two numbers with 6 point precision NN.NNNNNN
 * @property longitude Longitude (ordinate) of the stop point. Formatted as two numbers with 6 point precision NN.NNNNNN
 * @property shortIdentifier Short identifier from Jore4, combined letter + number. XXNNNN
 *
 * @constructor Create a Hastus Stop with given values
 */
data class Stop(
    val identifier: String,
    val platform: String,
    val descriptionFinnish: String,
    val descriptionSwedish: String,
    val streetFinnish: String,
    val streetSwedish: String,
    val place: String?,
    val latitude: NumberWithAccuracy,
    val longitude: NumberWithAccuracy,
    val shortIdentifier: String
) : IExportableItem {

    override fun getFields(): List<Any> = listOf(
        identifier,
        platform,
        descriptionFinnish,
        descriptionSwedish,
        streetFinnish,
        streetSwedish,
        place ?: "",
        latitude,
        longitude,
        shortIdentifier
    )
}
