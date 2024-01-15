package fi.hsl.jore4.hastus.api

import com.ninjasquad.springmockk.MockkBean
import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.config.WebSecurityConfig
import fi.hsl.jore4.hastus.service.exporting.ExportService
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
    fun `returns the associated status code when processing fails due to any ResponseStatusException`() {
        val resultErrorMessage = "Something unexpected happened"

        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, resultErrorMessage)

        executeExportRoutesRequest()
            .andExpect(status().isIAmATeapot)
            .andExpect(content().string(resultErrorMessage))
            .andExpect(content().contentType("text/plain;charset=iso-8859-1"))
    }

    @Test
    fun `returns 500 when processing fails due to an unknown error`() {
        val resultErrorMessage = "Something unexpected happened"

        every {
            exportService.exportRoutes(any(), any(), any(), any())
        } throws Exception(resultErrorMessage)

        executeExportRoutesRequest()
            .andExpect(status().isInternalServerError)
            // FIXME: should probably return a sensible content.
            .andExpect(content().string("status"))
            .andExpect(content().contentType("text/plain;charset=iso-8859-1"))
    }
}
