package fi.hsl.jore4.hastus.test

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.hsl.jore4.hastus.api.ExportController
import fi.hsl.jore4.hastus.data.format.Coordinate
import fi.hsl.jore4.hastus.generated.scalars.AnyToCoordinateConverter
import fi.hsl.jore4.hastus.generated.scalars.AnyToDurationConverter
import fi.hsl.jore4.hastus.generated.scalars.AnyToIJSONBConverter
import fi.hsl.jore4.hastus.generated.scalars.AnyToLocalDateConverter
import fi.hsl.jore4.hastus.generated.scalars.AnyToOffsetDateTimeConverter
import fi.hsl.jore4.hastus.generated.scalars.AnyToUUIDConverter
import fi.hsl.jore4.hastus.generated.scalars.AnyToUUIDListConverter
import fi.hsl.jore4.hastus.generated.scalars.CoordinateToAnyConverter
import fi.hsl.jore4.hastus.generated.scalars.DurationToAnyConverter
import fi.hsl.jore4.hastus.generated.scalars.IJSONBToAnyConverter
import fi.hsl.jore4.hastus.generated.scalars.LocalDateToAnyConverter
import fi.hsl.jore4.hastus.generated.scalars.OffsetDateTimeToAnyConverter
import fi.hsl.jore4.hastus.generated.scalars.UUIDListToAnyConverter
import fi.hsl.jore4.hastus.generated.scalars.UUIDToAnyConverter
import fi.hsl.jore4.hastus.graphql.IJSONB
import fi.hsl.jore4.hastus.graphql.UUIDList
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@DisplayName("Test the object mapper")
class ObjectMapperTest {

    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    data class UUIDFormat(
        @JsonSerialize(converter = UUIDToAnyConverter::class)
        @JsonDeserialize(converter = AnyToUUIDConverter::class)
        val uuid: UUID
    )
    data class UUIDListFormat(
        @JsonSerialize(converter = UUIDListToAnyConverter::class)
        @JsonDeserialize(converter = AnyToUUIDListConverter::class)
        val uuidList: UUIDList
    )
    data class DateFormat(
        @JsonSerialize(converter = LocalDateToAnyConverter::class)
        @JsonDeserialize(converter = AnyToLocalDateConverter::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        val date: LocalDate
    )
    data class OffsetTimeFormat(
        @JsonSerialize(converter = OffsetDateTimeToAnyConverter::class)
        @JsonDeserialize(converter = AnyToOffsetDateTimeConverter::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
        val offsetTime: OffsetDateTime
    )
    data class IjsonbFormat(
        @JsonSerialize(converter = IJSONBToAnyConverter::class)
        @JsonDeserialize(converter = AnyToIJSONBConverter::class)
        val ijsonb: IJSONB
    )
    data class CoordinateFormat(
        @JsonSerialize(converter = CoordinateToAnyConverter::class)
        @JsonDeserialize(converter = AnyToCoordinateConverter::class)
        val coordinate: Coordinate
    )
    data class DurationFormat(
        @JsonSerialize(converter = DurationToAnyConverter::class)
        @JsonDeserialize(converter = AnyToDurationConverter::class)
        val duration: java.time.Duration
    )

    @Test
    @DisplayName("When forming JSON")
    fun testJsonForm() {
        val jsonString = """
        {
            "uniqueLabels": ["65x", "65y"],
            "priority": 20,
            "observationDate": "2022-10-24"
        }
        """.trimMargin()

        val routes: ExportController.Routes = objectMapper.readValue(jsonString)

        assertEquals(2, routes.uniqueLabels.size)
        assertEquals("65x", routes.uniqueLabels[0])
        assertEquals("65y", routes.uniqueLabels[1])
        assertEquals(20, routes.priority)
        assertEquals(LocalDate.of(2022, 10, 24), routes.observationDate)
    }

    @Nested
    @DisplayName("Mapping to JSON")
    inner class MappingToJson() {

        @Test
        @DisplayName("Test UUID mapping")
        fun testUUIDMapping() {
            val value = UUID.randomUUID()
            val formatted = objectMapper.writeValueAsString(UUIDFormat(value))
            val expected = """
            {"uuid":"$value"}
            """.trimIndent()
            assertEquals(expected, formatted)
        }

        @Test
        @DisplayName("Test UUIDList mapping")
        fun testUUIDListMapping() {
            val value = listOf(UUID.randomUUID(), UUID.randomUUID())
            val expected = """
            {"uuidList":"{${value[0]},${value[1]}}"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(UUIDListFormat(UUIDList(value)))
            assertEquals(expected, formatted)
        }

        @Test
        @DisplayName("Test date mapping")
        fun testDateMapping() {
            val value = LocalDate.of(2022, 2, 2)
            val expected = """
            {"date":"2022-02-02"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(DateFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        @DisplayName("Test offset time mapping")
        fun testOffsetTimeMapping() {
            val value = OffsetDateTime.of(2022, 2, 2, 1, 2, 3, 4000000, ZoneOffset.UTC)
            val expected = """
            {"offsetTime":"2022-02-02 01:02:03.004 +0000"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(OffsetTimeFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        @DisplayName("Test IJSONB mapping")
        fun testIJSONBMapping() {
            val value = IJSONB(mapOf("first" to "value", "second" to "other"))
            val expected = """
            {"ijsonb":{"first":"value","second":"other"}}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(IjsonbFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        @DisplayName("Test coordinate mapping")
        fun testCoordinateMapping() {
            val value = Coordinate(1.0, 2.0)
            val expected = """
            {"coordinate":{"type":"Point","crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:EPSG::4326"}},"coordinates":[${value.x},${value.y},0.0]}}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(CoordinateFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        @DisplayName("Test duration mapping")
        fun testDurationMapping() {
            val value: Duration = 4.hours
            val expected = """
            {"duration":"${value.toIsoString()}"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(DurationFormat(value.toJavaDuration()))
            assertEquals(expected, formatted)
        }
    }

    @Nested
    @DisplayName("Mapping from JSON")
    inner class MappingFromJson() {

        @Test
        @DisplayName("Test UUID mapping")
        fun testUUIDMapping() {
            val value = UUID.randomUUID()
            val jsonString = """
            {
                "uuid": "$value"
            }
            """.trimIndent()
            val parsed: UUIDFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.uuid)
        }

        @Test
        @DisplayName("Test UUIDList mapping")
        fun testUUIDListMapping() {
            val value = listOf(UUID.randomUUID(), UUID.randomUUID())
            val jsonString = """
            {
                "uuidList": "{${value[0]}, ${value[1]}}"
            }
            """.trimIndent()
            val parsed: UUIDListFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.uuidList.content)
        }

        @Test
        @DisplayName("Test date mapping")
        fun testDateMapping() {
            val value = LocalDate.of(2022, 2, 2)
            val jsonString = """
            {
                "date": "2022-02-02"
            }
            """.trimIndent()
            val parsed: DateFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.date)
        }

        @Test
        @DisplayName("Test offset time mapping")
        fun testOffsetTimeMapping() {
            val value = OffsetDateTime.of(2022, 2, 2, 1, 2, 3, 4000000, ZoneOffset.UTC)
            val jsonString = """
            {
                "offsetTime": "2022-02-02 01:02:03.004 +0000"
            }
            """.trimIndent()
            val parsed: OffsetTimeFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.offsetTime)
        }

        @Test
        @DisplayName("Test IJSONB mapping")
        fun testIJSONBMapping() {
            val value = mapOf("first" to "value", "second" to "other")
            val jsonString = """
            {
                "ijsonb": {
                    "first": "value",
                    "second": "other"
                }
            }
            """.trimIndent()
            val parsed: IjsonbFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.ijsonb.content)
        }

        @Test
        @DisplayName("Test coordinate mapping")
        fun testCoordinateMapping() {
            val value = Coordinate(1.0, 2.0)
            val jsonString = """
             {
                "coordinate": {
                    "type": "Point",
                    "crs": {
                        "type": "name",
                        "properties": {
                            "name": "urn:ogc:def:crs:EPSG::4326"
                        }
                    },
                    "coordinates": [
                        ${value.x},
                        ${value.y},
                        0.0
                    ]
                }
            }
            """.trimIndent()
            val parsed: CoordinateFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.coordinate)
        }

        @Test
        @DisplayName("Test duration mapping")
        fun testDurationMapping() {
            val value: Duration = 4.hours
            val jsonString = """
            {
                "duration": "${value.toIsoString()}"
            }
            """.trimIndent()
            val parsed: DurationFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.duration.toKotlinDuration())
        }
    }
}
