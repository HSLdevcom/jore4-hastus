package fi.hsl.jore4.hastus.data.hastus

import fi.hsl.jore4.hastus.Constants
import java.time.format.DateTimeFormatter

sealed class HastusData : IHastusData {

    companion object {
        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern(Constants.DEFAULT_HASTUS_DATE_FORMAT)

        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern(Constants.DEFAULT_HASTUS_TIME_FORMAT)

        @JvmStatic
        protected fun dateFormatter(): DateTimeFormatter = DATE_FORMATTER

        @JvmStatic
        protected fun timeFormatter(): DateTimeFormatter = TIME_FORMATTER

        @JvmStatic
        protected fun parseToDouble(string: String): Double {
            return string.trim().toDoubleOrNull() ?: 0.0
        }

        @JvmStatic
        protected fun parseToInt(string: String): Int {
            return string.trim().toIntOrNull() ?: 0
        }

        @JvmStatic
        protected fun parseToBoolean(string: String) = parseToInt(string) > 0
    }
}
