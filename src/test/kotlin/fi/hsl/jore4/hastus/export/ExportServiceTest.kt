package fi.hsl.jore4.hastus.export

import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.graphql.FetchRoutesResult
import fi.hsl.jore4.hastus.graphql.GraphQLService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class ExportServiceTest {

    @MockK
    lateinit var graphQLService: GraphQLService

    lateinit var exportService: ExportService

    @BeforeEach
    fun setupServiceUnderTest() {
        exportService = ExportService(graphQLService, true)
    }

    @DisplayName("Validate deep-fetched routes got from GraphQLService.deepFetchRoutes(...)")
    @Nested
    inner class ValidateDeepFetchedRoutes {

        private fun stubDeepFetchRoutesForValidationSideEffects(line: JoreLine): FetchRoutesResult {
            val fetchRoutesResult = FetchRoutesResult(listOf(line), emptyList(), emptyList(), emptyList())

            // given
            every {
                graphQLService.deepFetchRoutes(any(), any(), any(), any())
            } /* then */ returns fetchRoutesResult

            return fetchRoutesResult
        }

        private fun invokeExportRoutesWithAnyParameters() {
            // Because of the stubbing done in stubDeepFetchRoutesForValidationSideEffects() the
            // parameter values used here are not meaningful. Anything goes.
            exportService.exportRoutes(listOf(), 10, LocalDate.now(), emptyMap())
        }

        @DisplayName("Validation should succeed when the first and the last stop points are timing points")
        @Test
        fun smoke() {
            val stopPoints = listOf(
                createFirstStopPoint("1KALA"),
                createFirstStopPoint("1ELIEL")
            )
            val line = createLine(stopPoints)

            stubDeepFetchRoutesForValidationSideEffects(line)

            invokeExportRoutesWithAnyParameters()
        }

        @DisplayName("When the journey pattern consists of less than two stop points")
        @Nested
        inner class WhenThereAreLessThanTwoStopPoints {

            @DisplayName("When there is only one stop point in journey pattern")
            @Test
            fun whenFirstStopPointIsNotTimingPointAndDoesNotHaveTimingPlaceAssociation() {
                val line = createLine(
                    listOf(
                        createFirstStopPoint("1KALA")
                        // no other stop points given, just one
                    )
                )

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<TooFewStopPointsException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }
        }

        @DisplayName("When the first stop point in journey pattern is not a valid timing point")
        @Nested
        inner class WhenFirstStopPointIsNotTimingPoint {

            @DisplayName("When the first stop point is not a timing point and does not have timing place name")
            @Test
            fun whenFirstStopPointIsNotTimingPointAndDoesNotHaveTimingPlaceName() {
                val stopPoints = listOf(
                    createFirstStopPoint(null, false),
                    createLastStopPoint("1ELIEL")
                )
                val line = createLine(stopPoints)

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<FirstStopNotTimingPointException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }

            @DisplayName("When the first stop point is a timing point but does not have timing place name")
            @Test
            fun whenFirstStopPointIsTimingPointButDoesNotHaveTimingPlaceName() {
                val stopPoints = listOf(
                    createFirstStopPoint(null, true),
                    createLastStopPoint("1ELIEL")
                )
                val line = createLine(stopPoints)

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<FirstStopNotTimingPointException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }

            @DisplayName("When the first stop point is not a timing point but has timing place name")
            @Test
            fun whenFirstStopPointIsNotTimingPointButHasTimingPlaceName() {
                val stopPoints = listOf(
                    createFirstStopPoint("1KALA", false),
                    createLastStopPoint("1ELIEL")
                )
                val line = createLine(stopPoints)

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<FirstStopNotTimingPointException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }
        }

        @DisplayName("When the last stop point in journey pattern is not a valid timing point")
        @Nested
        inner class WhenLastStopPointIsNotTimingPoint {

            @DisplayName("When the last stop point is not a timing point and does not have timing place name")
            @Test
            fun whenLastStopPointIsNotTimingPointAndDoesNotHaveTimingPlaceName() {
                val stopPoints = listOf(
                    createFirstStopPoint("1KALA"),
                    createLastStopPoint(null, false)
                )
                val line = createLine(stopPoints)

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<LastStopNotTimingPointException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }

            @DisplayName("When the last stop point is a timing point but does not have timing place name")
            @Test
            fun whenLastStopPointIsTimingPointButDoesNotHaveTimingPlaceName() {
                val stopPoints = listOf(
                    createFirstStopPoint("1ELIEL"),
                    createLastStopPoint(null, true)
                )
                val line = createLine(stopPoints)

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<LastStopNotTimingPointException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }

            @DisplayName("When the last stop point is not a timing point but has timing place name")
            @Test
            fun whenLastStopPointIsNotTimingPointButHasTimingPlaceName() {
                val stopPoints = listOf(
                    createFirstStopPoint("1KALA"),
                    createLastStopPoint("1ELIEL", false)
                )
                val line = createLine(stopPoints)

                stubDeepFetchRoutesForValidationSideEffects(line)

                assertFailsWith<LastStopNotTimingPointException> {
                    invokeExportRoutesWithAnyParameters()
                }
            }
        }
    }

    companion object {

        fun createLine(stopsOnRoute: List<JoreRouteScheduledStop>): JoreLine {
            return JoreLine(
                label = "65",
                "Rautatientori - Veräjälaakso FI",
                0,
                listOf(
                    JoreRoute(
                        label = "65x",
                        variant = "",
                        uniqueLabel = "65x",
                        name = "Reitti A - B FI",
                        direction = 1,
                        reversible = false,
                        stopsOnRoute = stopsOnRoute
                    )
                )
            )
        }

        fun createFirstStopPoint(
            timingPlaceShortName: String?,
            isTimingPoint: Boolean = true
        ): JoreRouteScheduledStop {
            return JoreRouteScheduledStop(
                timingPlaceShortName = timingPlaceShortName,
                distanceToNextStop = 123.0,
                isRegulatedTimingPoint = false,
                isAllowedLoad = false,
                isTimingPoint = isTimingPoint,
                stopLabel = "H1000"
            )
        }

        fun createLastStopPoint(
            timingPlaceShortName: String?,
            isTimingPoint: Boolean = true
        ): JoreRouteScheduledStop {
            return JoreRouteScheduledStop(
                timingPlaceShortName = timingPlaceShortName,
                distanceToNextStop = 0.0,
                isRegulatedTimingPoint = false,
                isAllowedLoad = false,
                isTimingPoint = isTimingPoint,
                stopLabel = "H9999"
            )
        }
    }
}
