package fi.hsl.jore4.hastus.data

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy

/**
 * Hastus Stop point representing a Jore4 scheduled stop point
 *
 * @property identifier Stop label as a 7 numbers long string NNNNNNN
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
class Stop(
    private val identifier: String,
    private val platform: String,
    private val descriptionFinnish: String,
    private val descriptionSwedish: String,
    private val streetFinnish: String,
    private val streetSwedish: String,
    private val place: String,
    gpsX: Number,
    gpsY: Number,
    private val shortIdentifier: String
) : HastusData() {

    private val gpsX: NumberWithAccuracy
    private val gpsY: NumberWithAccuracy

    init {
        this.gpsX = NumberWithAccuracy(gpsX, 2, 6)
        this.gpsY = NumberWithAccuracy(gpsY, 2, 6)
    }

    override val fieldName = "stop"

    override fun getFields(): List<Any> {
        return listWithFieldName(
            identifier,
            platform,
            descriptionFinnish,
            descriptionSwedish,
            streetFinnish,
            streetSwedish,
            place,
            gpsX,
            gpsY,
            shortIdentifier
        )
    }
}
