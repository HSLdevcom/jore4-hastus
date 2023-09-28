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

    data class OffsetDateTimeFormat(
        @JsonSerialize(converter = OffsetDateTimeToAnyConverter::class)
        @JsonDeserialize(converter = AnyToOffsetDateTimeConverter::class)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS Z")
        val offsetDateTime: OffsetDateTime
    )

    data class IJsonbFormat(
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
        fun `test UUID mapping`() {
            val value = UUID.randomUUID()
            val formatted = objectMapper.writeValueAsString(UUIDFormat(value))
            val expected = """
            {"uuid":"$value"}
            """.trimIndent()
            assertEquals(expected, formatted)
        }

        @Test
        fun `test UUIDList mapping`() {
            val value = listOf(UUID.randomUUID(), UUID.randomUUID())
            val expected = """
            {"uuidList":"{${value[0]},${value[1]}}"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(UUIDListFormat(UUIDList(value)))
            assertEquals(expected, formatted)
        }

        @Test
        fun `test date mapping`() {
            val value = LocalDate.of(2022, 2, 2)
            val expected = """
            {"date":"2022-02-02"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(DateFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        fun `test offset time mapping`() {
            val value = OffsetDateTime.of(2022, 2, 2, 1, 2, 3, 4000000, ZoneOffset.UTC)
            val expected = """
            {"offsetDateTime":"2022-02-02 01:02:03.004 +0000"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(OffsetDateTimeFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        fun `test IJSONB mapping`() {
            val value = IJSONB(mapOf("first" to "value", "second" to "other"))
            val expected = """
            {"ijsonb":{"first":"value","second":"other"}}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(IJsonbFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        fun `test coordinate mapping`() {
            val value = Coordinate(1.0, 2.0)
            val expected = """
            {"coordinate":{"type":"Point","crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:EPSG::4326"}},"coordinates":[${value.x},${value.y},0.0]}}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(CoordinateFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        fun `test duration mapping`() {
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
        fun `test UUID mapping`() {
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
        fun `test UUIDList mapping`() {
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
        fun `test date mapping`() {
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
        fun `test offset time mapping`() {
            val value = OffsetDateTime.of(2022, 2, 2, 1, 2, 3, 4000000, ZoneOffset.UTC)
            val jsonString = """
            {
                "offsetDateTime": "2022-02-02 01:02:03.004 +0000"
            }
            """.trimIndent()
            val parsed: OffsetDateTimeFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.offsetDateTime)
        }

        @Test
        fun `test IJSONB mapping`() {
            val value = mapOf("first" to "value", "second" to "other")
            val jsonString = """
            {
                "ijsonb": {
                    "first": "value",
                    "second": "other"
                }
            }
            """.trimIndent()
            val parsed: IJsonbFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.ijsonb.content)
        }

        @Test
        fun `test coordinate mapping`() {
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
        fun `test duration mapping`() {
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
