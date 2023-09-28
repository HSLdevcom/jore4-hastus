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

@DisplayName("Test JacksonObjectMapper")
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
    @DisplayName("When parsing JSON")
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
    @DisplayName("Test serialising Object types to JSON")
    inner class TestSerialisingObjectTypesToJson {

        @Test
        fun `format UUID as JSON`() {
            val value = UUID.randomUUID()
            val formatted = objectMapper.writeValueAsString(UUIDFormat(value))
            val expected = """
            {"uuid":"$value"}
            """.trimIndent()
            assertEquals(expected, formatted)
        }

        @Test
        fun `format UUIDList as JSON`() {
            val value = listOf(UUID.randomUUID(), UUID.randomUUID())
            val expected = """
            {"uuidList":"{${value[0]},${value[1]}}"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(UUIDListFormat(UUIDList(value)))
            assertEquals(expected, formatted)
        }

        @Test
        fun `format LocalDate as JSON`() {
            val value = LocalDate.of(2022, 2, 2)
            val expected = """
            {"date":"2022-02-02"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(DateFormat(value))
            assertEquals(expected, formatted)
        }

        @Nested
        @DisplayName("format OffsetDateTime to JSON")
        inner class TestSerialisingOffsetDateTimeToJson {

            @Test
            fun `with UTC zone`() {
                val value = OffsetDateTime.of(2022, 2, 2, 1, 2, 3, 4000000, ZoneOffset.UTC)

                assertEquals(
                    """
                    {"offsetDateTime":"2022-02-02T01:02:03.004Z"}
                    """.trimIndent(),
                    objectMapper.writeValueAsString(OffsetDateTimeFormat(value))
                )
            }

            @Test
            fun `with timezone for Finnish summer time`() {
                val zoneOffset = ZoneOffset.of("+03:00")
                val value = OffsetDateTime.of(2022, 2, 2, 1, 2, 3, 4000000, zoneOffset)

                assertEquals(
                    """
                    {"offsetDateTime":"2022-02-02T01:02:03.004+03:00"}
                    """.trimIndent(),
                    objectMapper.writeValueAsString(OffsetDateTimeFormat(value))
                )
            }
        }

        @Test
        fun `format IJSONB as JSON`() {
            val value = IJSONB(mapOf("first" to "value", "second" to "other"))
            val expected = """
            {"ijsonb":{"first":"value","second":"other"}}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(IJsonbFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        fun `format Coordinate as GeoJSON`() {
            val value = Coordinate(1.0, 2.0)
            val expected = """
            {"coordinate":{"type":"Point","crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:EPSG::4326"}},"coordinates":[${value.x},${value.y},0.0]}}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(CoordinateFormat(value))
            assertEquals(expected, formatted)
        }

        @Test
        fun `format Duration as JSON`() {
            val value: Duration = 4.hours
            val expected = """
            {"duration":"${value.toIsoString()}"}
            """.trimIndent()
            val formatted = objectMapper.writeValueAsString(DurationFormat(value.toJavaDuration()))
            assertEquals(expected, formatted)
        }
    }

    @Nested
    @DisplayName("Test deserialising Object types from JSON")
    inner class TestDeserialisingObjectTypesFromJson() {

        @Test
        fun `parse UUID from JSON`() {
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
        fun `parse UUIDList from JSON`() {
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
        fun `parse LocalDate from JSON`() {
            val value = LocalDate.of(2022, 2, 2)
            val jsonString = """
            {
                "date": "2022-02-02"
            }
            """.trimIndent()
            val parsed: DateFormat = objectMapper.readValue(jsonString)
            assertEquals(value, parsed.date)
        }

        @Nested
        @DisplayName("parse OffsetDateTime from JSON")
        inner class TestDeserialisingOffsetDateTimeFromJSON {

            @Nested
            @DisplayName("with UTC timezone")
            inner class WithUtcTimezone {

                private fun createOffsetDateTime(nanoOfSecond: Int) = OffsetDateTime
                    .of(2022, 2, 2, 1, 2, 3, nanoOfSecond, ZoneOffset.UTC)

                @Test
                fun `without fractions of seconds`() {
                    assertEquals(
                        createOffsetDateTime(0),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03Z"}"""
                        ).offsetDateTime
                    )
                }

                @Test
                fun `with deci-seconds`() {
                    assertEquals(
                        createOffsetDateTime(400_000_000),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03.4Z"}"""
                        ).offsetDateTime
                    )
                }

                @Test
                fun `with centi-seconds`() {
                    assertEquals(
                        createOffsetDateTime(40_000_000),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03.04Z"}"""
                        ).offsetDateTime
                    )
                }

                @Test
                fun `with milliseconds`() {
                    assertEquals(
                        createOffsetDateTime(4_000_000),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03.004Z"}"""
                        ).offsetDateTime
                    )
                }
            }

            @Nested
            @DisplayName("with offset timezone")
            inner class WithOffsetTimezone {

                private val zoneOffset = ZoneOffset.of("+03:00")

                private fun createOffsetDateTime(nanoOfSecond: Int) = OffsetDateTime
                    .of(2022, 2, 2, 1, 2, 3, nanoOfSecond, zoneOffset)

                @Test
                fun `without fractions of seconds`() {
                    assertEquals(
                        createOffsetDateTime(0),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03+03:00"}"""
                        ).offsetDateTime
                    )
                }

                @Test
                fun `with deci-seconds`() {
                    assertEquals(
                        createOffsetDateTime(400_000_000),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03.4+03:00"}"""
                        ).offsetDateTime
                    )
                }

                @Test
                fun `with centi-seconds`() {
                    assertEquals(
                        createOffsetDateTime(40_000_000),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03.04+03:00"}"""
                        ).offsetDateTime
                    )
                }

                @Test
                fun `with milliseconds`() {
                    assertEquals(
                        createOffsetDateTime(4_000_000),
                        objectMapper.readValue<OffsetDateTimeFormat>(
                            """{"offsetDateTime": "2022-02-02T01:02:03.004+03:00"}"""
                        ).offsetDateTime
                    )
                }
            }
        }

        @Test
        fun `parse IJSONB from JSON`() {
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
        fun `parse Coordinate from GeoJSON`() {
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
        fun `parse Duration from JSON`() {
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
