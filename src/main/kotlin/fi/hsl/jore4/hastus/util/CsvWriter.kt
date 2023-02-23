package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class CsvWriter(
    private val separator: String = ";",
    private val decimal: Char = '.'
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
        return data.getFields().joinToString(separator = separator, transform = { safeStringTransform(it) })
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
