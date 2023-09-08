package fi.hsl.jore4.hastus.export

import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.export.validation.ExportStopPointsValidator
import fi.hsl.jore4.hastus.export.validation.IExportLineValidator
import fi.hsl.jore4.hastus.graphql.FetchRoutesResult
import fi.hsl.jore4.hastus.graphql.GraphQLService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class ExportServiceTest : ExportTestDataCreator {

    // Validator is spied in order to verify interactions.
    val lineValidator: IExportLineValidator = spyk(ExportStopPointsValidator(true))

    @MockK
    lateinit var graphQLService: GraphQLService

    lateinit var exportService: ExportService

    @BeforeEach
    fun setupServiceUnderTest() {
        exportService = ExportService(graphQLService, lineValidator)
    }

    @DisplayName("Validate deep-fetched routes got from GraphQLService.deepFetchRoutes(...)")
    @Nested
    inner class ValidateDeepFetchedRoutes {

        private fun mockGraphQLService(line: JoreLine) {
            val fetchRoutesResult = FetchRoutesResult(listOf(line), emptyList(), emptyList(), emptyList())

            // given
            every {
                graphQLService.deepFetchRoutes(any(), any(), any(), any())
            } /* then */ returns fetchRoutesResult
        }

        private fun invokeExportRoutes() {
            // Because of the stubbing done in stubDeepFetchRoutesForValidationSideEffects() the
            // parameter values used here are not meaningful. Anything goes.
            exportService.exportRoutes(listOf(), 10, LocalDate.now(), emptyMap())
        }

        @Test
        fun `validation should succeed when the first and the last stop points are timing points`() {
            val stopPoints: List<JoreRouteScheduledStop> = listOf(
                createFirstStopPoint("1KALA"),
                createFirstStopPoint("1ELIEL")
            )
            val line: JoreLine = createLine(stopPoints)

            mockGraphQLService(line)

            invokeExportRoutes()

            verify(exactly = 1) { lineValidator.validateLine(line) }
        }

        @Test
        fun `validation should fail when the first or the last stop point is not a timing point`() {
            val stopPoints: List<JoreRouteScheduledStop> = listOf(
                createFirstStopPoint(null, false),
                createFirstStopPoint("1ELIEL", true)
            )
            val line: JoreLine = createLine(stopPoints)

            mockGraphQLService(line)

            assertFailsWith<RuntimeException> {
                invokeExportRoutes()
            }

            verify(exactly = 1) { lineValidator.validateLine(line) }
        }
    }
}
