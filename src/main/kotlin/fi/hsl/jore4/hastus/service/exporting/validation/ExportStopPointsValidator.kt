package fi.hsl.jore4.hastus.service.exporting.validation

import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreStopPointInJourneyPattern
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val LOGGER = KotlinLogging.logger {}

/**
 * Line validator implementation that validates only the stop points contained inside the object
 * hierarchy.
 *
 * @param failOnTimingPointValidation Boolean value indicating whether an exception should be
 * thrown on a failed timing point validation
 */
@Service
class ExportStopPointsValidator(
    @Value("\${failOnTimingPointValidation}") val failOnTimingPointValidation: Boolean
) : IExportLineValidator {
    /**
     * Validates stop points from the given line before they are exported.
     *
     * @param [line] The line object, within which the stop points are examined are validated
     *
     * @throws TooFewStopPointsException if there are less than two stop points on some journey
     * pattern belonging to the lines
     * @throws FirstStopNotTimingPointException if the first stop point is not a timing point
     * @throws LastStopNotTimingPointException if the last stop point is not a timing point
     */
    override fun validateLine(line: JoreLine) {
        line.routes.forEach { route ->

            if (route.stopPointsInJourneyPattern.size < 2) {
                LOGGER.warn {
                    "Journey pattern for route ${route.label} contains less than two stop points"
                }
                if (failOnTimingPointValidation) {
                    throw TooFewStopPointsException(route.label)
                }
            }

            val firstStopOnRoute: JoreStopPointInJourneyPattern = route.stopPointsInJourneyPattern.first()

            if (!firstStopOnRoute.isUsedAsTimingPoint || firstStopOnRoute.timingPlaceCode == null) {
                LOGGER.warn {
                    "The first stop point of the journey pattern for route ${route.label} is not a valid " +
                        "timing point as mandated by Hastus"
                }
                if (failOnTimingPointValidation) {
                    throw FirstStopNotTimingPointException(route.label)
                }
            }

            val lastStopOnRoute: JoreStopPointInJourneyPattern = route.stopPointsInJourneyPattern.last()

            if (!lastStopOnRoute.isUsedAsTimingPoint || lastStopOnRoute.timingPlaceCode == null) {
                LOGGER.warn {
                    "The last stop point of the journey pattern for route ${route.label} is not a valid " +
                        "timing point as mandated by Hastus"
                }
                if (failOnTimingPointValidation) {
                    throw LastStopNotTimingPointException(route.label)
                }
            }
        }
    }
}
