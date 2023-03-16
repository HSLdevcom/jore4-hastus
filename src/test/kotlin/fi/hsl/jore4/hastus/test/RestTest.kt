package fi.hsl.jore4.hastus.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.hsl.jore4.hastus.api.ExportController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

@DisplayName("Test the rest API")
class RestTest {

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Nested
    @DisplayName("Object mapper")
    inner class ObjectMapperTest() {
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

            assertEquals("65x", routes.uniqueLabels[0])
            assertEquals("65y", routes.uniqueLabels[1])
            assertEquals(20, routes.priority)
            assertEquals(LocalDate.of(2022, 10, 24), routes.observationDate)
        }
    }
}
