package fi.hsl.jore4.hastus.export.validation

import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.export.ExportTestDataCreator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ExportStopPointsValidatorTest : ExportTestDataCreator {

    @DisplayName("Validation should succeed when the first and the last stop points are timing points")
    @Test
    fun whenNoValidationErrorsPresent() {
        val stopPoints: List<JoreRouteScheduledStop> = listOf(
            createFirstStopPoint("1KALA"),
            createFirstStopPoint("1ELIEL")
        )
        val line: JoreLine = createLine(stopPoints)

        // should not throw exception
        validateLine(line)
    }

    @DisplayName("When the journey pattern consists of less than two stop points")
    @Nested
    inner class WhenThereAreLessThanTwoStopPoints {

        private fun createLineToTest(): JoreLine = createLine(
            listOf(
                createFirstStopPoint("1KALA")
                // no other stop points given, just one
            )
        )

        @DisplayName("When there is only one stop point in journey pattern")
        @Test
        fun whenFirstStopPointIsNotTimingPointAndDoesNotHaveTimingPlaceAssociation() {
            assertFailsWith<TooFewStopPointsException> {
                validateLine(createLineToTest())
            }
        }

        @DisplayName("When boolean flag for throwing exception is not set")
        @Test
        fun whenBooleanFlagForThrowingExceptionIsNotSet() {
            // should not throw exception
            validateSingleLineAndNeverExpectException(createLineToTest())
        }
    }

    @DisplayName("When the first stop point in journey pattern is not a valid timing point")
    @Nested
    inner class WhenFirstStopPointIsNotTimingPoint {

        private fun createLineWithFirstStopPointNotAsTimingPoint(): JoreLine = createLine(
            listOf(
                createFirstStopPoint(null, false),
                createLastStopPoint("1ELIEL")
            )
        )

        @DisplayName("When the first stop point is not a timing point and does not have timing place name")
        @Test
        fun whenFirstStopPointIsNotTimingPointAndDoesNotHaveTimingPlaceName() {
            assertFailsWith<FirstStopNotTimingPointException> {
                validateLine(createLineWithFirstStopPointNotAsTimingPoint())
            }
        }

        @DisplayName("When the first stop point is a timing point but does not have timing place name")
        @Test
        fun whenFirstStopPointIsTimingPointButDoesNotHaveTimingPlaceName() {
            val stopPoints: List<JoreRouteScheduledStop> = listOf(
                createFirstStopPoint(null, true),
                createLastStopPoint("1ELIEL")
            )
            val line: JoreLine = createLine(stopPoints)

            assertFailsWith<FirstStopNotTimingPointException> {
                validateLine(line)
            }
        }

        @DisplayName("When the first stop point is not a timing point but has timing place name")
        @Test
        fun whenFirstStopPointIsNotTimingPointButHasTimingPlaceName() {
            val stopPoints: List<JoreRouteScheduledStop> = listOf(
                createFirstStopPoint("1KALA", false),
                createLastStopPoint("1ELIEL")
            )
            val line: JoreLine = createLine(stopPoints)

            assertFailsWith<FirstStopNotTimingPointException> {
                validateLine(line)
            }
        }

        @DisplayName("When boolean flag for throwing exception is not set")
        @Test
        fun whenBooleanFlagForThrowingExceptionIsNotSet() {
            // should not throw exception
            validateSingleLineAndNeverExpectException(createLineWithFirstStopPointNotAsTimingPoint())
        }
    }

    @DisplayName("When the last stop point in journey pattern is not a valid timing point")
    @Nested
    inner class WhenLastStopPointIsNotTimingPoint {

        private fun createLineWithLastStopPointNotAsTimingPoint(): JoreLine = createLine(
            listOf(
                createFirstStopPoint("1KALA"),
                createLastStopPoint(null, false)
            )
        )

        @DisplayName("When the last stop point is not a timing point and does not have timing place name")
        @Test
        fun whenLastStopPointIsNotTimingPointAndDoesNotHaveTimingPlaceName() {
            assertFailsWith<LastStopNotTimingPointException> {
                validateLine(createLineWithLastStopPointNotAsTimingPoint())
            }
        }

        @DisplayName("When the last stop point is a timing point but does not have timing place name")
        @Test
        fun whenLastStopPointIsTimingPointButDoesNotHaveTimingPlaceName() {
            val stopPoints: List<JoreRouteScheduledStop> = listOf(
                createFirstStopPoint("1ELIEL"),
                createLastStopPoint(null, true)
            )
            val line: JoreLine = createLine(stopPoints)

            assertFailsWith<LastStopNotTimingPointException> {
                validateLine(line)
            }
        }

        @DisplayName("When the last stop point is not a timing point but has timing place name")
        @Test
        fun whenLastStopPointIsNotTimingPointButHasTimingPlaceName() {
            val stopPoints: List<JoreRouteScheduledStop> = listOf(
                createFirstStopPoint("1KALA"),
                createLastStopPoint("1ELIEL", false)
            )
            val line: JoreLine = createLine(stopPoints)

            assertFailsWith<LastStopNotTimingPointException> {
                validateLine(line)
            }
        }

        @DisplayName("When boolean flag for throwing exception is not set")
        @Test
        fun whenBooleanFlagForThrowingExceptionIsNotSet() {
            // should not throw exception
            validateSingleLineAndNeverExpectException(createLineWithLastStopPointNotAsTimingPoint())
        }
    }

    companion object {

        private fun validateLine(line: JoreLine) = validateLine(line, true)

        private fun validateSingleLineAndNeverExpectException(line: JoreLine) = validateLine(line, false)

        private fun validateLine(line: JoreLine, throwExceptionOnFailedTimingPointValidation: Boolean) {
            ExportStopPointsValidator(throwExceptionOnFailedTimingPointValidation)
                .validateLine(line)
        }
    }
}
