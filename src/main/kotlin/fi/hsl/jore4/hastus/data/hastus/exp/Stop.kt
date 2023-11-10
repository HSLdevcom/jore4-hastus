package fi.hsl.jore4.hastus.data.hastus.exp

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy

/**
 * Hastus Stop point representing a Jore4 scheduled stop point
 *
 * @property identifier Stop label
 * @property platform Platform number of the stop. Formatted as two numbers NN
 * @property descriptionFinnish Stop name in Finnish
 * @property descriptionSwedish Stop name in Swedish
 * @property streetFinnish Street the stop is located on in Finnish
 * @property streetSwedish Street the stop is located on in Swedish
 * @property place Name of the location specified in Jore4, if there is one. Format as XXXXXX
 * @property gpsX X coordinate of the stop. Formatted as two numbers with 6 point precision NN.NNNNNN
 * @property gpsY Y coordinate of the stop. Formatted as two numbers with 6 point precision NN.NNNNNN
 * @property shortIdentifier Short identifier from Jore4, combined letter + number. XXNNNN
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
    val gpsX: NumberWithAccuracy,
    val gpsY: NumberWithAccuracy,
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
        gpsX,
        gpsY,
        shortIdentifier
    )
}
