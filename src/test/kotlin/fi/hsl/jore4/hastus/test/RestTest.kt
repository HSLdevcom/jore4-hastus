package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.api.ExportController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Test the rest API")
class RestTest {

    val ec: ExportController = ExportController()

    @Nested
    @DisplayName("GET request")
    inner class GetRequest() {

        @Test
        @DisplayName("When a GET request is sent")
        fun whenDoingGetRequest() {
            assert(ec.helloWorld().size == 1)
        }
    }
}
