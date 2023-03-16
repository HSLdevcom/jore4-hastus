package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.util.UUID

class UUIDScalarConverter : ScalarConverter<UUID> {
    override fun toJson(value: UUID) = value.toString()
    override fun toScalar(rawValue: Any): UUID {
        if (rawValue is String) {
            return UUID.fromString(rawValue)
        }
        throw java.lang.IllegalArgumentException("UUID converter got non-string value: $rawValue")
    }
}
