package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy
import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.Place
import fi.hsl.jore4.hastus.data.hastus.Route
import fi.hsl.jore4.hastus.data.hastus.RouteVariant
import fi.hsl.jore4.hastus.data.hastus.RouteVariantPoint
import fi.hsl.jore4.hastus.data.hastus.Stop
import fi.hsl.jore4.hastus.data.hastus.StopDistance
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CsvWriter(
    private val separator: String = ";",
    decimal: Char = '.'
) {

    private val decimalFormat: DecimalFormat

    init {
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = decimal
        this.decimalFormat = DecimalFormat("0", symbols)
        decimalFormat.isGroupingUsed = false
    }

    fun transformToCsv(data: List<IHastusData>): String {
        return data.joinToString(System.lineSeparator()) { transformToCsvLine(it) }
    }

    fun transformToCsvLine(data: IHastusData): String {
        return (listOf(hastusFieldName(data)) + data.getFields())
            .joinToString(separator = separator, transform = { safeStringTransform(it) })
    }

    private fun hastusFieldName(data: IHastusData): String {
        return when (data) {
            is ApplicationRecord -> "1"
            is BlockRecord -> "4"
            is BookingRecord -> "2"
            is Place -> "place"
            is Route -> "route"
            is RouteVariant -> "rvariant"
            is RouteVariantPoint -> "rvpoint"
            is Stop -> "stop"
            is StopDistance -> "stpdist"
            is TripRecord -> "5"
            is TripStopRecord -> "6"
            is VehicleScheduleRecord -> "3"
        }
    }

    // Use pre specified decimal point
    private fun safeNumberTransform(number: Number): String {
        decimalFormat.applyLocalizedPattern("0")
        return decimalFormat.format(number)
    }

    private fun booleanTransform(value: Boolean): String {
        return if (value) "1" else "0"
    }

    private fun formattedNumberTransform(number: NumberWithAccuracy): String {
        decimalFormat.applyLocalizedPattern(number.getPattern())
        return decimalFormat.format(number.value)
    }

    // Remove all separators from strings which would break the CSV
    private fun safeStringTransform(item: Any): String {
        return when (item) {
            is NumberWithAccuracy -> formattedNumberTransform(item)
            is Number -> safeNumberTransform(item)
            is Boolean -> booleanTransform(item)
            else -> item.toString().replace(separator, "")
        }
    }
}
