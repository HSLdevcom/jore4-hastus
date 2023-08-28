package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternRef
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.graphql.GraphQLServiceFactory
import fi.hsl.jore4.hastus.service.exporting.validation.IExportLineValidator
import fi.hsl.jore4.hastus.util.CsvWriter
import fi.hsl.jore4.hastus.util.DateTimeUtil
import fi.hsl.jore4.hastus.util.DateTimeUtil.toOffsetDateTimeAtDefaultZone
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.OffsetDateTime

private val LOGGER = KotlinLogging.logger {}

@Service
class ExportService(
    val graphQLServiceFactory: GraphQLServiceFactory,
    val lineValidator: IExportLineValidator
) {

    private val writer = CsvWriter()

    /**
     * Export routes constrained by the parameters in Hastus CSV format. As a side effect, new
     * journey pattern references are persisted.
     *
     * @param [routeLabels] The labels of the routes to export
     * @param [priority] The priority used to constrain the routes to be exported
     * @param [observationDate] The date used to filter active/valid routes
     * @param [hasuraHeaders] Filtered HTTP headers from the request to pass to GraphQL client
     *
     * @throws RuntimeException if any validation errors are present.
     */
    fun exportRoutes(
        routeLabels: List<String>,
        priority: Int,
        observationDate: LocalDate,
        hasuraHeaders: Map<String, String>
    ): String {
        val uniqueRouteLabels: List<String> = routeLabels.distinct()
        val graphQLService: GraphQLService = graphQLServiceFactory.createForSession(hasuraHeaders)

        val (
            lines: List<JoreLine>,
            stopPoints: List<JoreScheduledStop>,
            timingPlaces: List<JoreTimingPlace>,
            distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints>
        ) =
            graphQLService.deepFetchRoutes(uniqueRouteLabels, priority, observationDate)

        // validate lines
        lines.forEach { lineValidator.validateLine(it) }

        val hastusData: List<IHastusData> =
            ConversionsToHastus.convertJoreLinesToHastus(lines) +
                ConversionsToHastus.convertJoreStopsToHastus(stopPoints) +
                ConversionsToHastus.convertJoreTimingPlacesToHastus(timingPlaces) +
                ConversionsToHastus.convertDistancesBetweenStopPointsToHastus(distancesBetweenStopPoints)

        // Extract routes for journey pattern ref creation.
        val routes: List<JoreRoute> = lines.flatMap { it.routes }

        createJourneyPatternRefs(routes, observationDate, graphQLService)

        return hastusData
            .distinct()
            .joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }

    private fun createJourneyPatternRefs(
        routes: List<JoreRoute>,
        observationDate: LocalDate,
        graphQLService: GraphQLService
    ): List<JoreJourneyPatternRef> {
        val snapshotTime = DateTimeUtil.currentDateTimeAtDefaultZone()
        val observationTime: OffsetDateTime = observationDate.toOffsetDateTimeAtDefaultZone()

        val journeyPatternRefs: List<JoreJourneyPatternRef> =
            graphQLService.createJourneyPatternReferences(routes, observationTime, snapshotTime)

        LOGGER.debug {
            "Persisted the following journey pattern refs during export: $journeyPatternRefs"
        }

        return journeyPatternRefs
    }
}
