package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.graphql.GraphQLServiceFactory
import fi.hsl.jore4.hastus.util.CsvReader
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

private val LOGGER = KotlinLogging.logger {}

@Service
class ImportService(private val graphQLServiceFactory: GraphQLServiceFactory) {

    fun importTimetablesFromCsv(
        csv: String,
        hasuraHeaders: Map<String, String>
    ): UUID? {
        val hastusItems: List<IHastusData> = READER.parseCsv(csv)
        val graphQLService: GraphQLService = graphQLServiceFactory.createForSession(hasuraHeaders)

        val hastusBookingRecord: BookingRecord = hastusItems.filterIsInstance<BookingRecord>().first()

        val hastusTrips: List<TripRecord> = hastusItems.filterIsInstance<TripRecord>()
        val hastusTripStops: List<TripStopRecord> = hastusItems.filterIsInstance<TripStopRecord>()

        val uniqueRouteLabels: List<String> = hastusTrips
            .map(ConversionsFromHastus::extractRouteLabel)
            .distinct() // TODO: is distinct operation really required?

        val journeyPatterns: List<JoreJourneyPattern> = graphQLService.getJourneyPatterns(
            uniqueRouteLabels,
            hastusBookingRecord.startDate,
            hastusBookingRecord.endDate
        )
        LOGGER.debug { "Fetched journey patterns: $journeyPatterns" }

        val vehicleTypeIndex: Map<Int, UUID> = graphQLService.getVehicleTypes()
        LOGGER.debug { "Using vehicle types $vehicleTypeIndex" }

        val dayTypeIndex: Map<String, UUID> = graphQLService.getDayTypes()
        LOGGER.debug { "Using day types $dayTypeIndex" }

        val journeyPatternIndex: Map<RouteLabelAndDirection, JoreJourneyPattern> =
            findMatchingJourneyPatternForEachHastusTrip(journeyPatterns, hastusTrips, hastusTripStops)

        val vehicleScheduleFrame: JoreVehicleScheduleFrame = ConversionsFromHastus.convertHastusDataToJore(
            hastusItems,
            vehicleTypeIndex,
            dayTypeIndex,
            journeyPatternIndex
        )

        val selectedJourneyPatterns: List<JoreJourneyPattern> = journeyPatternIndex.values.toList()

        return graphQLService.persistVehicleScheduleFrame(vehicleScheduleFrame, selectedJourneyPatterns)
    }

    companion object {

        private val READER = CsvReader(";")

        internal fun findMatchingJourneyPatternForEachHastusTrip(
            journeyPatterns: List<JoreJourneyPattern>,
            hastusTripRecords: List<TripRecord>,
            hastusTripStopRecords: List<TripStopRecord>
        ): Map<RouteLabelAndDirection, JoreJourneyPattern> {
            val results: MutableMap<RouteLabelAndDirection, JoreJourneyPattern> = mutableMapOf()

            val hastusRouteLabelsAndDirections: Set<RouteLabelAndDirection> =
                hastusTripRecords.map(ConversionsFromHastus::extractRouteLabelAndDirection).toSet()

            val journeyPatternsGroupedByRouteLabelAndDirection: Map<RouteLabelAndDirection, List<JoreJourneyPattern>> =
                journeyPatterns.groupBy { it.routeLabelAndDirection }

            verifyRouteLabelsAndDirectionsAreMatched(
                hastusRouteLabelsAndDirections,
                journeyPatternsGroupedByRouteLabelAndDirection.keys
            )

            val hastusTripStopRecordsIndexedByInternalNumber: Map<String, List<TripStopRecord>> =
                hastusTripStopRecords.groupBy { it.tripInternalNumber }

            hastusTripRecords.forEach { hastusTrip ->
                val hastusRouteLabelAndDirection: RouteLabelAndDirection =
                    ConversionsFromHastus.extractRouteLabelAndDirection(hastusTrip)

                val hastusTripStops: List<TripStopRecord> =
                    hastusTripStopRecordsIndexedByInternalNumber[hastusTrip.tripInternalNumber]
                        ?: run {
                            val errorMessage =
                                "No trip stop records found for Hastus trip: $hastusRouteLabelAndDirection"
                            LOGGER.warn(errorMessage)
                            throw InvalidHastusDataException(errorMessage)
                        }

                val hastusStopLabels: List<String> = hastusTripStops.map { it.stopId }

                val firstMatchingJourneyPattern: JoreJourneyPattern =
                    journeyPatternsGroupedByRouteLabelAndDirection[hastusRouteLabelAndDirection]
                        .orEmpty() // not really empty because of previously done label-direction matching
                        .firstOrNull { journeyPattern ->
                            // TODO The sorting order is currently non-deterministic and it is very
                            //  difficult to refine the intended sorting order based on the current
                            //  import logic. The situation will improve soon, when the export and
                            //  import flows are reflected again.

                            val joreStopLabels: List<String> = journeyPattern.stops.map { it.label }

                            joreStopLabels == hastusStopLabels
                        }
                        ?: run {
                            val exception = NoJourneyPatternMatchesHastusTripStopsException(hastusRouteLabelAndDirection)
                            LOGGER.warn(exception.message)
                            throw exception
                        }

                results[hastusRouteLabelAndDirection] = firstMatchingJourneyPattern
            }

            return results
        }

        private fun verifyRouteLabelsAndDirectionsAreMatched(
            labelsAndDirectionsFromHastus: Set<RouteLabelAndDirection>,
            labelsAndDirectionsFromJore: Set<RouteLabelAndDirection>
        ) {
            val missingRouteLabelsAndDirections: List<RouteLabelAndDirection> = labelsAndDirectionsFromHastus
                .subtract(labelsAndDirectionsFromJore)
                .sorted()

            if (missingRouteLabelsAndDirections.isNotEmpty()) {
                val exception = UnmatchedRoutesWithinImport(missingRouteLabelsAndDirections)
                LOGGER.warn(exception.message)
                throw exception
            }
        }
    }
}
