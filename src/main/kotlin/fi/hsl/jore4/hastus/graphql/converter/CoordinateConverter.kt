package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.hsl.jore4.hastus.data.format.Coordinate

class CoordinateConverter : ScalarConverter<Coordinate> {
    override fun toJson(value: Coordinate): ObjectNode {
        val formattedValue = OBJECT_MAPPER.writeValueAsString(
            mapOf(
                "type" to "Point",
                "crs" to
                    mapOf(
                        "type" to "name",
                        "properties" to
                            mapOf(
                                "name" to "urn:ogc:def:crs:EPSG::4326"
                            )
                    ),
                "coordinates" to listOf(value.x, value.y, 0.0)
            )
        )

        return OBJECT_MAPPER.readTree(formattedValue) as ObjectNode
    }

    override fun toScalar(rawValue: Any): Coordinate {
        if (rawValue !is Map<*, *>) {
            throw IllegalArgumentException("Location converter got non-Map value: $rawValue")
        }

        when (val coordinatesObj: Any? = rawValue["coordinates"]) {
            is List<*> -> {
                val x = coordinatesObj[0]
                val y = coordinatesObj[1]

                if (x is Double && y is Double) {
                    return Coordinate(x, y)
                }

                throw IllegalArgumentException("'coordinates' contains non-double values: $x and $y")
            }

            else -> throw IllegalArgumentException("'coordinates' is not a List: $coordinatesObj")
        }
    }

    companion object {
        private val OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
    }
}
