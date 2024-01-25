package fi.hsl.jore4.hastus.api

import com.ninjasquad.springmockk.MockkBean
import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.api.util.HastusApiErrorType
import fi.hsl.jore4.hastus.config.WebSecurityConfig
import fi.hsl.jore4.hastus.service.exporting.ExportService
import fi.hsl.jore4.hastus.service.exporting.validation.FirstStopNotTimingPointException
import fi.hsl.jore4.hastus.service.exporting.validation.LastStopNotTimingPointException
import fi.hsl.jore4.hastus.service.exporting.validation.TooFewStopPointsException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@WebMvcTest(ExportController::class)
@Import(WebSecurityConfig::class)
@ExtendWith(MockKExtension::class)
class ExportControllerTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var exportService: ExportService

    private fun executeExportRoutesRequest(
        uniqueLabels: List<String> = listOf("123"),
        priority: Int = 10,
        observationDate: LocalDate = LocalDate.of(2023, 1, 23),
        hasuraHeadersMap: Map<String, String> = emptyMap()
    ): ResultActions {
        val hasuraHeaders = HttpHeaders().also { headers ->
            hasuraHeadersMap.forEach { (name, value) ->
                headers.set(name, value)
            }
        }

        return mockMvc
            .perform(
                post("/export/routes")
                    .headers(hasuraHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "uniqueLabels": $uniqueLabels,
                          "priority": $priority,
                          "observationDate": "$observationDate"
                        }
                        """.trimIndent()
                    )
            )
    }

    @Test
    fun `returns 200 and a correct response when called successfully`() {
        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } answers { "<some_csv_content>" }

        executeExportRoutesRequest(listOf("123", "456"), 10, LocalDate.of(2023, 1, 23))
            .andExpect(status().isOk)
            .andExpect(
                content().string("<some_csv_content>")
            )
            .andExpect(content().contentType(MIME_TYPE_CSV))

        verify {
            exportService.exportRoutes(listOf("123", "456"), 10, LocalDate.of(2023, 1, 23), mapOf())
        }
        verify(exactly = 1) {
            exportService.exportRoutes(any(), any(), any(), any())
        }
    }

    @Test
    fun `returns 400 when validation fails due to first stop not being a timing point`() {
        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws FirstStopNotTimingPointException("123")

        executeExportRoutesRequest()
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(
                    HastusApiErrorType.FirstStopNotTimingPointError,
                    "The first stop point in the journey pattern for route 123 is not a timing point"
                )
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `returns 400 when validation fails due to last stop not being a timing point`() {
        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws LastStopNotTimingPointException("123")

        executeExportRoutesRequest()
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(
                    HastusApiErrorType.LastStopNotTimingPointError,
                    "The last stop point in the journey pattern for route 123 is not a timing point"
                )
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `returns 400 when validation fails due to there being too few stop points`() {
        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws TooFewStopPointsException("123")

        executeExportRoutesRequest()
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(
                    HastusApiErrorType.TooFewStopPointsError,
                    "There are less than two stops points in the journey pattern for route 123"
                )
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `returns the associated status code when processing fails due to any ResponseStatusException`() {
        val resultErrorMessage = "Something unexpected happened"

        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, resultErrorMessage)

        executeExportRoutesRequest()
            .andExpect(status().isIAmATeapot)
            .andExpect(
                constructExpectedErrorBody(
                    HastusApiErrorType.UnknownError,
                    resultErrorMessage
                )
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `returns 500 when processing fails due to an unknown error`() {
        val resultErrorMessage = "Something unexpected happened"

        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws Exception(resultErrorMessage)

        executeExportRoutesRequest()
            .andExpect(status().isInternalServerError)
            .andExpect(
                constructExpectedErrorBody(
                    HastusApiErrorType.UnknownError,
                    resultErrorMessage
                )
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    companion object {
        private fun constructExpectedErrorBody(type: HastusApiErrorType, errorMessage: String): ResultMatcher {
            return content().json(
                """
                {
                    "reason": "$errorMessage",
                    "type": "$type"
                }
                """.trimIndent(),
                true
            )
        }
    }
}
