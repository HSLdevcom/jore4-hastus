package fi.hsl.jore4.hastus.api

import com.ninjasquad.springmockk.MockkBean
import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.api.util.HastusApiErrorType
import fi.hsl.jore4.hastus.config.WebSecurityConfig
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.graphql.converter.GraphQLAuthenticationFailedException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByRouteLabelAndDirectionException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByStopPointLabelsException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByTimingPlaceLabelsException
import fi.hsl.jore4.hastus.service.importing.ErrorWhileProcessingHastusDataException
import fi.hsl.jore4.hastus.service.importing.ImportService
import fi.hsl.jore4.hastus.service.importing.InvalidHastusDataException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(ImportController::class)
@Import(WebSecurityConfig::class)
@ExtendWith(MockKExtension::class)
class ImportControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc
    ) {
        @MockkBean
        private lateinit var importService: ImportService

        private fun executeImportTimetablesRequest(
            csv: String,
            hasuraHeadersMap: Map<String, String> = emptyMap()
        ): ResultActions {
            val hasuraHeaders =
                HttpHeaders().also { headers ->
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
                ).andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                    constructExpectedErrorBody(
                        HastusApiErrorType.InvalidHastusDataError,
                        resultErrorMessage
                    )
                )

            verify(exactly = 1) {
                importService.importTimetablesFromCsv(any(), any())
            }
        }

        @Test
        fun `returns 400 when not able to find journey pattern reference whose route label and direction match Hastus trip`() {
            every {
                importService.importTimetablesFromCsv(any(), any())
            } throws
                CannotFindJourneyPatternRefByRouteLabelAndDirectionException(
                    listOf(
                        RouteLabelAndDirection("123", JoreRouteDirection.OUTBOUND),
                        RouteLabelAndDirection("456", JoreRouteDirection.INBOUND)
                    )
                )

            executeImportTimetablesRequest("<csv_content>")
                .andExpect(status().isBadRequest)
                .andExpect(
                    constructExpectedErrorBody(
                        HastusApiErrorType.CannotFindJourneyPatternRefByRouteLabelAndDirectionError,
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
            } throws
                CannotFindJourneyPatternRefByStopPointLabelsException(
                    RouteLabelAndDirection("123", JoreRouteDirection.OUTBOUND),
                    listOf("H1000", "H1001", "H1002")
                )

            executeImportTimetablesRequest("<csv_content>")
                .andExpect(status().isBadRequest)
                .andExpect(
                    constructExpectedErrorBody(
                        HastusApiErrorType.CannotFindJourneyPatternRefByStopPointLabelsError,
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
            } throws
                CannotFindJourneyPatternRefByTimingPlaceLabelsException(
                    RouteLabelAndDirection("123", JoreRouteDirection.OUTBOUND),
                    listOf("H1000", "H1001", "H1002"),
                    listOf("1PLACE", null, "2PLACE")
                )

            executeImportTimetablesRequest("<csv_content>")
                .andExpect(status().isBadRequest)
                .andExpect(
                    constructExpectedErrorBody(
                        HastusApiErrorType.CannotFindJourneyPatternRefByTimingPlaceLabelsError,
                        """
                        Could not find matching journey pattern reference whose timing place labels correspond to the Hastus trip.

                        Trip label: 123,
                        Trip direction: 1,
                        Stop points with place codes: [H1000:1PLACE, H1001, H1002:2PLACE]
                        """.trimIndent()
                    )
                )

            verify(exactly = 1) {
                importService.importTimetablesFromCsv(any(), any())
            }
        }

        // This should actually never happen but the output format is tested anyway. Basically, this is
        // because this exception is raised from the data transformation/conversion step (Hastus
        // booking record -> Jore vehicle schedule frame), which happens after the corresponding journey
        // pattern reference is found for each Hastus trip in the preceding association step. Basically,
        // there is redundant error checking and errors related to this exception should be caught in
        // the first mentioned step, at least for now.
        @Test
        fun `returns 500 when error encountered while processing Hastus data`() {
            every {
                importService.importTimetablesFromCsv(any(), any())
            } throws ErrorWhileProcessingHastusDataException("encountered an error")

            executeImportTimetablesRequest("<csv_content>")
                .andExpect(status().isInternalServerError)
                .andExpect(
                    constructExpectedErrorBody(
                        HastusApiErrorType.ErrorWhileProcessingHastusDataError,
                        "encountered an error"
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
                    constructExpectedErrorBody(
                        HastusApiErrorType.GraphQLAuthenticationFailedError,
                        resultErrorMessage
                    )
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
                    constructExpectedErrorBody(
                        HastusApiErrorType.UnknownError,
                        resultErrorMessage
                    )
                )

            verify(exactly = 1) {
                importService.importTimetablesFromCsv(any(), any())
            }
        }

        companion object {
            private fun constructExpectedErrorBody(
                type: HastusApiErrorType,
                errorMessage: String
            ): ResultMatcher =
                content().json(
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
