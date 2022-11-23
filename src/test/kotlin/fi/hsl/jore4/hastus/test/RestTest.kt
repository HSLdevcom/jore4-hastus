package fi.hsl.jore4.hastus.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired

@DisplayName("Test the rest API")
class RestTest @Autowired constructor() {

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    /*
    val ec: ExportController = ExportController()

    @Nested
    @DisplayName("GET request")
    inner class GetRequest() {

        @Test
        @DisplayName("When a GET request is sent")
        fun whenDoingGetRequest() {
            // assert(ec.helloWorld().size == 1)
        }

        @Test
        @DisplayName("When forming JSON")
        fun testJsonForm() {
            val jsonString = """
            {
                "labels": ["65x", "65y"],
                "priority": 20,
                "observationDate": "2022-10-24"
            }
            """.trimMargin()

            val routes: ExportController.Routes = objectMapper.readValue(jsonString)

            assert(routes.labels[0] == "65x")
            assert(routes.labels[1] == "65y")
            assert(routes.priority == 20)
            assert(routes.observationDate == LocalDate.of(2022, 10, 24))
        }
    }
     */
}
