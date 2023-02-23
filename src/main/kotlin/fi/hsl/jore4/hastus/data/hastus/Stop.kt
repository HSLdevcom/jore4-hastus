package fi.hsl.jore4.hastus.data.hastus

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stop

        if (identifier != other.identifier) return false
        if (platform != other.platform) return false
        if (descriptionFinnish != other.descriptionFinnish) return false
        if (descriptionSwedish != other.descriptionSwedish) return false
        if (streetFinnish != other.streetFinnish) return false
        if (streetSwedish != other.streetSwedish) return false
        if (place != other.place) return false
        if (shortIdentifier != other.shortIdentifier) return false
        if (gpsX != other.gpsX) return false
        if (gpsY != other.gpsY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + platform.hashCode()
        result = 31 * result + descriptionFinnish.hashCode()
        result = 31 * result + descriptionSwedish.hashCode()
        result = 31 * result + streetFinnish.hashCode()
        result = 31 * result + streetSwedish.hashCode()
        result = 31 * result + place.hashCode()
        result = 31 * result + shortIdentifier.hashCode()
        result = 31 * result + gpsX.hashCode()
        result = 31 * result + gpsY.hashCode()
        return result
    }

    override fun toString(): String {
        return "Stop(identifier='$identifier', platform='$platform', descriptionFinnish='$descriptionFinnish', descriptionSwedish='$descriptionSwedish', streetFinnish='$streetFinnish', streetSwedish='$streetSwedish', place='$place', shortIdentifier='$shortIdentifier', gpsX=$gpsX, gpsY=$gpsY, fieldName='$fieldName')"
    }
}
