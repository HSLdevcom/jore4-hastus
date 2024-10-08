package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.hsl.jore4.hastus.graphql.IJSONB

class JsonbScalarConverter : ScalarConverter<IJSONB> {
    override fun toJson(value: IJSONB): Any {
        val formattedValue = jacksonObjectMapper().writeValueAsString(value.content)
        return jacksonObjectMapper().readTree(formattedValue) as ObjectNode
    }

    override fun toScalar(rawValue: Any): IJSONB {
        if (rawValue !is java.util.LinkedHashMap<*, *>) {
            throw java.lang.IllegalArgumentException("jsonb converter got non-map value: $rawValue")
        }
        return IJSONB(content = rawValue.map { it.key.toString() to it.value.toString() }.toMap())
    }
}
