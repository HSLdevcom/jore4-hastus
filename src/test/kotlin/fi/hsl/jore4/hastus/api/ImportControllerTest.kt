package fi.hsl.jore4.hastus.api

import com.ninjasquad.springmockk.MockkBean
import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.graphql.converter.GraphQLAuthenticationFailedException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByRouteLabelAndDirectionException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByStopPointLabelsException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByTimingPlaceLabelsException
import fi.hsl.jore4.hastus.service.importing.ImportService
import fi.hsl.jore4.hastus.service.importing.InvalidHastusDataException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@ExtendWith(MockKExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class ImportControllerTest @Autowired constructor(
    val mockMvc: MockMvc
) {

    @MockkBean
    private lateinit var importService: ImportService

    private fun executeImportTimetablesRequest(
        csv: String,
        hasuraHeadersMap: Map<String, String> = emptyMap()
    ): ResultActions {
        val hasuraHeaders = HttpHeaders().also { headers ->
            hasuraHeadersMap.forEach { (name, value) ->
                headers.set(name, value)
            }
        }

        return mockMvc
            .perform(
                post("/import")
                    .headers(hasuraHeaders)
                    .contentType(MIME_TYPE_CSV)
                    .content(csv)
            )
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `returns 200 and a correct response when called successfully`() {
        val resultVehicleScheduleFrameId = UUID.randomUUID()

        every {
            importService.importTimetablesFromCsv(any(), any())
        } answers { resultVehicleScheduleFrameId }

        executeImportTimetablesRequest("<some_csv_content>")
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "vehicleScheduleFrameId": $resultVehicleScheduleFrameId
                    }
                    """.trimIndent(),
                    true
                )
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    @Test
    fun `returns 400 when trying to import invalid Hastus data`() {
        val resultErrorMessage = "Invalid data"

        every {
            importService.importTimetablesFromCsv(any(), any())
        } throws InvalidHastusDataException(resultErrorMessage)

        executeImportTimetablesRequest("<invalid_csv_content>")
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(resultErrorMessage)
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    @Test
    fun `returns 400 when not able to find journey pattern reference whose route label and direction match Hastus trip`() {
        every {
            importService.importTimetablesFromCsv(any(), any())
        } throws CannotFindJourneyPatternRefByRouteLabelAndDirectionException(
            listOf(
                RouteLabelAndDirection("123", JoreRouteDirection.OUTBOUND),
                RouteLabelAndDirection("456", JoreRouteDirection.INBOUND)
            )
        )

        executeImportTimetablesRequest("<csv_content>")
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(
                    "Could not find journey pattern reference for Hastus trips with the following route " +
                        "labels and directions: 123 (outbound),456 (inbound)"
                )
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    @Test
    fun `returns 400 when not able to find journey pattern reference whose stop point labels match Hastus trip`() {
        every {
            importService.importTimetablesFromCsv(any(), any())
        } throws CannotFindJourneyPatternRefByStopPointLabelsException(
            RouteLabelAndDirection("123", JoreRouteDirection.OUTBOUND),
            listOf("H1000", "H1001", "H1002")
        )

        executeImportTimetablesRequest("<csv_content>")
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(
                    """
                    Could not find matching journey pattern reference whose stop points correspond to the Hastus trip.

                    Trip label: 123,
                    Trip direction: 1,
                    Stop points: [H1000, H1001, H1002]
                    """.trimIndent()
                )
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    @Test
    fun `returns 400 when not able to find journey pattern reference whose timing place labels match Hastus trip`() {
        every {
            importService.importTimetablesFromCsv(any(), any())
        } throws CannotFindJourneyPatternRefByTimingPlaceLabelsException(
            RouteLabelAndDirection("123", JoreRouteDirection.OUTBOUND),
            listOf("H1000", "H1001", "H1002"),
            listOf("1PLACE", null, "2PLACE")
        )

        executeImportTimetablesRequest("<csv_content>")
            .andExpect(status().isBadRequest)
            .andExpect(
                constructExpectedErrorBody(
                    """
                    Could not find matching journey pattern reference whose timing place labels correspond to the Hastus trip.

                    Trip label: 123,
                    Trip direction: 1,
                    Stop points: [H1000, H1001, H1002],
                    Place codes: [1PLACE, , 2PLACE]
                    """.trimIndent()
                )
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    @Test
    fun `returns 403 when authentication fails for GraphQL request`() {
        val resultErrorMessage = "authentication failed"

        every {
            importService.importTimetablesFromCsv(any(), any())
        } throws GraphQLAuthenticationFailedException(resultErrorMessage)

        executeImportTimetablesRequest("<csv_content>")
            .andExpect(status().isForbidden)
            .andExpect(
                constructExpectedErrorBody(resultErrorMessage)
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    @Test
    fun `returns 500 when processing fails due to other exception`() {
        val resultErrorMessage = "Something unexpected happened"

        every {
            importService.importTimetablesFromCsv(any(), any())
        } throws Exception(resultErrorMessage)

        executeImportTimetablesRequest("<csv_content>")
            .andExpect(status().isInternalServerError)
            .andExpect(
                constructExpectedErrorBody(resultErrorMessage)
            )

        verify(exactly = 1) {
            importService.importTimetablesFromCsv(any(), any())
        }
    }

    companion object {
        private fun constructExpectedErrorBody(errorMessage: String): ResultMatcher {
            return content().json(
                """
                {
                    "reason": "$errorMessage"
                }
                """.trimIndent(),
                true
            )
        }
    }
}
