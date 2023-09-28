package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class DateTimeScalarConverter : ScalarConverter<OffsetDateTime> {

    override fun toJson(value: OffsetDateTime): String = value.format(dateTimeFormatterForSerialising)

    override fun toScalar(rawValue: Any): OffsetDateTime {
        return when (rawValue) {
            is String -> OffsetDateTime.parse(rawValue, dateTimeFormatterForDeserialising)

            else -> throw IllegalArgumentException("Error parsing $rawValue as OffsetDateTime")
        }
    }

    companion object {

        private const val DATETIME_FORMAT_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

        private val dateTimeFormatterForSerialising = DateTimeFormatter.ofPattern(DATETIME_FORMAT_MILLISECONDS)

        /**
         * It turns out that GraphQL responses have a variable number of decimals in the
         * timestamps in terms of fractions of seconds. This happens because the trailing
         * zeros are removed from the decimal part. Therefore, we construct the formatter for
         * deserialising in such a way that variable number of digits in the decimal part is
         * supported.
         */
        private val dateTimeFormatterForDeserialising: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 3, true)
            .appendOffset("+HH:MM", "Z")
            .toFormatter()
    }
}
