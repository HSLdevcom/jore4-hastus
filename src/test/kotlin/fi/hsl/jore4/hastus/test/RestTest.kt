package fi.hsl.jore4.hastus.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.hsl.jore4.hastus.api.ExportController
import fi.hsl.jore4.hastus.graphql.GraphQLService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

@DisplayName("Test the rest API")
class RestTest @Autowired constructor() {

    val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    private val gqService = Mockito.mock(GraphQLService::class.java)

    val ec: ExportController = ExportController(gqService)

    @Nested
    @DisplayName("POST request")
    inner class PostRequest() {
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

            assert(routes.uniqueLabels[0] == "65x")
            assert(routes.uniqueLabels[1] == "65y")
            assert(routes.priority == 20)
            assert(routes.observationDate == LocalDate.of(2022, 10, 24))
        }
    }
}
