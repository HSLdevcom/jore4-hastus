package fi.hsl.jore4.hastus.export

import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.data.mapper.ConversionsToHastus
import fi.hsl.jore4.hastus.export.validation.IExportLineValidator
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.util.CsvWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ExportService @Autowired constructor(
    val graphQLService: GraphQLService,
    val lineValidator: IExportLineValidator
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
     * @throws RuntimeException if any validation errors are present.
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

        // validate lines
        lines.forEach { lineValidator.validateLine(it) }

        val hastusData: List<IHastusData> =
            ConversionsToHastus.convertJoreLinesToHastus(lines) +
                ConversionsToHastus.convertJoreStopsToHastus(stopPoints) +
                ConversionsToHastus.convertJoreTimingPlacesToHastus(timingPlaces) +
                ConversionsToHastus.convertDistancesBetweenStopPointsToHastus(distancesBetweenStopPoints)

        return hastusData
            .distinct()
            .joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }
}
