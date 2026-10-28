package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import fi.hsl.jore4.hastus.data.format.Coordinate
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode
import tools.jackson.module.kotlin.jacksonObjectMapper

class CoordinateConverter : ScalarConverter<Coordinate> {
    override fun toJson(value: Coordinate): ObjectNode {
        val formattedValue =
            OBJECT_MAPPER.writeValueAsString(
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
                    "coordinates" to listOf(value.longitude, value.latitude, 0.0)
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
                val lng = coordinatesObj[0]
                val lat = coordinatesObj[1]

                if (lng is Double && lat is Double) {
                    return Coordinate(longitude = lng, latitude = lat)
                }

                throw IllegalArgumentException("'coordinates' contains non-double values: $lng and $lat")
            }

            else -> {
                throw IllegalArgumentException("'coordinates' is not a List: $coordinatesObj")
            }
        }
    }

    companion object {
        private val OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
    }
}
