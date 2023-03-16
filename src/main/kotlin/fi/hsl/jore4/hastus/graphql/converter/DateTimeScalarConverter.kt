package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DateTimeScalarConverter : ScalarConverter<OffsetDateTime> {
    override fun toJson(value: OffsetDateTime): Any {
        return value.format(formatter)
    }

    override fun toScalar(rawValue: Any): OffsetDateTime {
        if (rawValue is String) {
            return OffsetDateTime.parse(rawValue, formatter)
        }
        throw IllegalArgumentException("Error parsing $rawValue as date, expected format $dateFormat")
    }

    companion object {
        const val dateFormat = "yyyy-MM-dd HH:mm:ss.SSS Z"

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    }
}
