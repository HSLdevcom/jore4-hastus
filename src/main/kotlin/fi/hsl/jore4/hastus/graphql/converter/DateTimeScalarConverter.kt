package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateTimeScalarConverter : ScalarConverter<OffsetDateTime> {

    override fun toJson(value: OffsetDateTime): String = value.format(millisecondFormatter)

    override fun toScalar(rawValue: Any): OffsetDateTime {
        when (rawValue) {
            is String -> {
                /**
                 * It turns out that GraphQL responses have a variable number of decimals in the
                 * timestamps in terms of fractions of seconds. This happens because the trailing
                 * zeros are removed from the decimal part.
                 *
                 * Therefore, we need to parse [OffsetDateTime]s by trying multiple
                 * [DateTimeFormatter]s in order to avoid [DateTimeParseException].
                 */

                for (formatter in fractionsOfSecondsFormatters) {
                    try {
                        return OffsetDateTime.parse(rawValue, formatter)
                    } catch (dpe: DateTimeParseException) {
                        continue
                    }
                }

                // last resort
                return OffsetDateTime.parse(rawValue, evenSecondsFormatter)
            }

            else -> throw IllegalArgumentException(
                "Error parsing $rawValue as OffsetDateTime, expected format $DATETIME_FORMAT_MILLISECONDS"
            )
        }
    }

    companion object {

        private const val DATETIME_FORMAT_EVEN_SECONDS = "yyyy-MM-dd'T'HH:mm:ssXXX"
        private const val DATETIME_FORMAT_DECI_SECONDS = "yyyy-MM-dd'T'HH:mm:ss.SXXX"
        private const val DATETIME_FORMAT_CENTI_SECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSXXX"
        private const val DATETIME_FORMAT_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

        private val evenSecondsFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT_EVEN_SECONDS)
        private val millisecondFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT_MILLISECONDS)

        private val fractionsOfSecondsFormatters: List<DateTimeFormatter> = listOf(
            millisecondFormatter,
            DateTimeFormatter.ofPattern(DATETIME_FORMAT_CENTI_SECONDS),
            DateTimeFormatter.ofPattern(DATETIME_FORMAT_DECI_SECONDS)
        )
    }
}
