package fi.hsl.jore4.hastus.data.format

/**
 * Number with accuracy
 *
 * @property value number value
 * @property leading number of leading numbers
 * @property digits number of digits
 */
data class NumberWithAccuracy(val value: Number, val leading: Int, val digits: Int)
