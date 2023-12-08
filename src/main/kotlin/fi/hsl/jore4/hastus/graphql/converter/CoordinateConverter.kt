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
                "coordinates" to listOf(value.longitude, value.latitude, 0.0)
            )
        )

        return OBJECT_MAPPER.readTree(formattedValue) as ObjectNode
    }

    override fun toScalar(rawValue: Any): Coordinate {
        if (rawValue !is LinkedHashMap<*, *>) {
            throw IllegalArgumentException("Location converter got non-map value: $rawValue")
        }
        if (rawValue["coordinates"] !is ArrayList<*>) {
            throw IllegalArgumentException("Location has non-map coordinates: " + rawValue["coordinates"])
        }

        val numbers = rawValue["coordinates"] as ArrayList<*>
        val longitude = numbers[0]
        val latitude = numbers[1]

        if (longitude is Double && latitude is Double) {
            return Coordinate(longitude, latitude)
        }

        throw IllegalArgumentException("Location has non-double coordinates: $longitude and $latitude")
    }

    companion object {
        private val OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
    }
}
