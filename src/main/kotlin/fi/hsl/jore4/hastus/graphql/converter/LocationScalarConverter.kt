package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.hsl.jore4.hastus.graphql.Coordinate

class LocationScalarConverter : ScalarConverter<Coordinate> {
    override fun toJson(value: Coordinate): String {
        return jacksonObjectMapper().writeValueAsString(
            mapOf(
                "type" to "Point",
                "crs" to mapOf(
                    "type" to "name",
                    "properties" to mapOf(
                        "name" to "urn:ogc:def:crs:EPSG::4326"
                    )
                ),
                "coordinates" to listOf(
                    value.x,
                    value.y,
                    0.0
                )
            )
        )
    }

    override fun toScalar(rawValue: Any): Coordinate {
        if (rawValue !is LinkedHashMap<*, *>) {
            throw IllegalArgumentException("Location converter got non-map value: $rawValue")
        }
        if (rawValue["coordinates"] !is ArrayList<*>) {
            throw IllegalArgumentException("Location has non-map coordinates: " + rawValue["coordinates"])
        }
        val numbers = rawValue["coordinates"] as ArrayList<*>
        val x = numbers[0]
        val y = numbers[1]
        if (x is Double && y is Double) {
            return Coordinate(x, y)
        }
        throw IllegalArgumentException("Location has non-double coordinates: $x and $y")
    }
}
