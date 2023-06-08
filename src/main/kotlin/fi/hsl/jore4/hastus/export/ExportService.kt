package fi.hsl.jore4.hastus.export

import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.data.mapper.ConversionsToHastus
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.util.CsvWriter
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate

private val LOGGER = KotlinLogging.logger {}

@Service
class ExportService @Autowired constructor(
    val graphQLService: GraphQLService,
    @Value("\${failOnTimingPointValidation}") val failOnTimingPointValidation: Boolean
) {

    private val writer = CsvWriter()

    /**
     * Export routes constrained by the parameters in Hastus CSV format.
     *
     * @param [uniqueRouteLabels] The labels of the routes to export
     * @param [priority] The priority used to constrain the routes to be exported
     * @param [observationDate] The date used to filter active/valid routes
     * @param [headers] HTTP headers from the request to be passed
     *
     * @throws TooFewStopPointsException if there are less than two stop points on some journey
     * pattern belonging to the lines
     * @throws FirstStopNotTimingPointException if the first stop point is not a timing point
     * @throws LastStopNotTimingPointException if the last stop point is not a timing point
     */
    fun exportRoutes(
        uniqueRouteLabels: List<String>,
        priority: Int,
        observationDate: LocalDate,
        headers: Map<String, String>
    ): String {
        val (
            lines: List<JoreLine>,
            stopPoints: List<JoreScheduledStop>,
            timingPlaces: List<JoreTimingPlace>,
            distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints>
        ) =
            graphQLService.deepFetchRoutes(uniqueRouteLabels, priority, observationDate, headers)

        // validate stop points
        validateStopPoints(lines, failOnTimingPointValidation)

        val hastusData: List<IHastusData> =
            ConversionsToHastus.convertJoreLinesToHastus(lines) +
                ConversionsToHastus.convertJoreStopsToHastus(stopPoints) +
                ConversionsToHastus.convertJoreTimingPlacesToHastus(timingPlaces) +
                ConversionsToHastus.convertDistancesBetweenStopPointsToHastus(distancesBetweenStopPoints)

        return hastusData
            .distinct()
            .joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }

    companion object {

        private fun validateStopPoints(lines: List<JoreLine>, failOnTimingPointValidation: Boolean) {
            lines.forEach { line ->
                line.routes.forEach { route ->

                    if (route.stopsOnRoute.size < 2) {
                        LOGGER.warn {
                            "Journey pattern for route ${route.label} contains less than two stop points"
                        }
                        if (failOnTimingPointValidation) {
                            throw TooFewStopPointsException(route.label)
                        }
                    }

                    if (route.stopsOnRoute.first().timingPlaceShortName == null) {
                        LOGGER.warn {
                            "The first stop point of the journey pattern for route ${route.label} is not a timing " +
                                "point as mandated by Hastus"
                        }
                        if (failOnTimingPointValidation) {
                            throw FirstStopNotTimingPointException(route.label)
                        }
                    }

                    if (route.stopsOnRoute.last().timingPlaceShortName == null) {
                        LOGGER.warn {
                            "The last stop point of the journey pattern for route ${route.label} is not a timing " +
                                "point as mandated by Hastus"
                        }
                        if (failOnTimingPointValidation) {
                            throw LastStopNotTimingPointException(route.label)
                        }
                    }
                }
            }
        }
    }
}
