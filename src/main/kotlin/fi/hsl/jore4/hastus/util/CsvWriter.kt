package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy
import fi.hsl.jore4.hastus.data.hastus.exp.IExportableItem
import fi.hsl.jore4.hastus.data.hastus.exp.Place
import fi.hsl.jore4.hastus.data.hastus.exp.Route
import fi.hsl.jore4.hastus.data.hastus.exp.RouteVariant
import fi.hsl.jore4.hastus.data.hastus.exp.RouteVariantPoint
import fi.hsl.jore4.hastus.data.hastus.exp.Stop
import fi.hsl.jore4.hastus.data.hastus.exp.StopDistance
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

    fun transformToCsv(data: List<IExportableItem>): String = data.joinToString(System.lineSeparator()) { transformToCsvLine(it) }

    fun transformToCsvLine(data: IExportableItem): String =
        (listOf(hastusFieldName(data)) + data.getFields())
            .joinToString(separator = separator, transform = { safeStringTransform(it) })

    private fun hastusFieldName(data: IExportableItem): String =
        when (data) {
            is Place -> "place"
            is Route -> "route"
            is RouteVariant -> "rvariant"
            is RouteVariantPoint -> "rvpoint"
            is Stop -> "stop"
            is StopDistance -> "stpdist"
        }

    // Use pre specified decimal point
    private fun safeNumberTransform(number: Number): String {
        decimalFormat.applyLocalizedPattern("0")
        return decimalFormat.format(number)
    }

    private fun booleanTransform(value: Boolean): String = if (value) "1" else "0"

    private fun formattedNumberTransform(number: NumberWithAccuracy): String {
        decimalFormat.applyLocalizedPattern(number.getPattern())
        return decimalFormat.format(number.value)
    }

    // Remove all separators from strings which would break the CSV
    private fun safeStringTransform(item: Any): String =
        when (item) {
            is Boolean -> booleanTransform(item)
            is Number -> safeNumberTransform(item)
            is NumberWithAccuracy -> formattedNumberTransform(item)
            else -> item.toString().replace(separator, "")
        }
}
