package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import fi.hsl.jore4.hastus.graphql.UUIDList
import java.util.UUID

class UUIDListScalarConverter : ScalarConverter<UUIDList> {

    override fun toJson(value: UUIDList): String {
        return value.content.joinToString(
            transform = { it.toString() },
            separator = ",",
            prefix = "{",
            postfix = "}"
        )
    }

    override fun toScalar(rawValue: Any): UUIDList {
        val stringValue = rawValue.toString()

        return UUIDList(
            stringValue.trim(' ', '{', '}')
                .split(',')
                .map { UUID.fromString(it.trim()) }
        )
    }
}
