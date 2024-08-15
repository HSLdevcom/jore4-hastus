package fi.hsl.jore4.hastus.data.format

/**
 * Number with accuracy
 *
 * @property value number value
 * @property leading number of leading numbers
 * @property digits number of digits
 */
data class NumberWithAccuracy(val value: Number, val leading: Int, val digits: Int) {
    // Get the pattern for this number format for a decimal formatter
    fun getPattern(): String {
        return if (leading > 0 && digits > 0) {
            "0".repeat(leading) + "." + "0".repeat(digits)
        } else if (leading > 0) {
            "0".repeat(leading)
        } else {
            "." + "0".repeat(digits)
        }
    }
}
