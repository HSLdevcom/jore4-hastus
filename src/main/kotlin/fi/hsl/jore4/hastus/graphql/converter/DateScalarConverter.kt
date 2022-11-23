package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateScalarConverter : ScalarConverter<LocalDate> {
    override fun toJson(value: LocalDate): String {
        return value.format(formatter)
    }

    override fun toScalar(rawValue: Any): LocalDate {
        if (rawValue is String) {
            return LocalDate.parse(rawValue, formatter)
        }
        throw IllegalArgumentException("Error parsing $rawValue as date, expected format $DATE_FORMAT")
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"

        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
    }
}
