package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter

class GenericConverter : ScalarConverter<Any> {
    override fun toJson(value: Any): Any = value

    override fun toScalar(rawValue: Any): Any = rawValue
}
