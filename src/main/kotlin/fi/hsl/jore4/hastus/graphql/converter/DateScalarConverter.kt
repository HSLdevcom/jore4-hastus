package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateScalarConverter : ScalarConverter<LocalDate> {
    override fun toJson(value: LocalDate): Any {
        return value.format(formatter)
    }

    override fun toScalar(rawValue: Any): LocalDate {
        if (rawValue is String) {
            return LocalDate.parse(rawValue, formatter)
        }
        throw IllegalArgumentException("Error parsing $rawValue as date, expected format $dateFormat")
    }

    companion object {
        const val dateFormat = "yyyy-MM-dd"

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    }
}
