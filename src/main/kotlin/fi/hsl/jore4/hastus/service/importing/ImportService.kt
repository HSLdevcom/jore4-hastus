package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternRef
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.graphql.GraphQLServiceFactory
import fi.hsl.jore4.hastus.util.CollectionUtil.filterOutConsecutiveDuplicates
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

        val journeyPatternRefs: List<JoreJourneyPatternRef> = graphQLService.getJourneyPatternReferences(
            uniqueRouteLabels,
            hastusBookingRecord.startDate,
            hastusBookingRecord.endDate
        )
        LOGGER.debug { "Fetched journey pattern references: $journeyPatternRefs" }

        val vehicleTypeIndex: Map<Int, UUID> = graphQLService.getVehicleTypes()
        LOGGER.debug { "Using vehicle types: $vehicleTypeIndex" }

        val dayTypeIndex: Map<String, UUID> = graphQLService.getDayTypes()
        LOGGER.debug { "Using day types: $dayTypeIndex" }

        val journeyPatternRefIndex: Map<RouteLabelAndDirection, JoreJourneyPatternRef> =
            findMatchingJourneyPatternRefForEachHastusTrip(journeyPatternRefs, hastusTrips, hastusTripStops)

        val vehicleScheduleFrame: JoreVehicleScheduleFrame = ConversionsFromHastus.convertHastusDataToJore(
            hastusItems,
            vehicleTypeIndex,
            dayTypeIndex,
            journeyPatternRefIndex
        )

        val selectedJourneyPatternRefs: Collection<JoreJourneyPatternRef> = journeyPatternRefIndex.values

        return graphQLService.persistVehicleScheduleFrame(vehicleScheduleFrame, selectedJourneyPatternRefs)
    }

    companion object {

        private val READER = CsvReader(";")

        internal fun findMatchingJourneyPatternRefForEachHastusTrip(
            journeyPatternsRefs: List<JoreJourneyPatternRef>,
            hastusTripRecords: List<TripRecord>,
            hastusTripStopRecords: List<TripStopRecord>
        ): Map<RouteLabelAndDirection, JoreJourneyPatternRef> {
            val results: MutableMap<RouteLabelAndDirection, JoreJourneyPatternRef> = mutableMapOf()

            val hastusRouteLabelsAndDirections: Set<RouteLabelAndDirection> =
                hastusTripRecords.map(ConversionsFromHastus::extractRouteLabelAndDirection).toSet()

            val journeyPatternRefsGroupedByRouteLabelAndDirection: Map<RouteLabelAndDirection, List<JoreJourneyPatternRef>> =
                journeyPatternsRefs.groupBy { it.routeLabelAndDirection }

            verifyRouteLabelsAndDirectionsAreMatched(
                hastusRouteLabelsAndDirections,
                journeyPatternRefsGroupedByRouteLabelAndDirection.keys
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

                // Consecutive pairs of stop points and Hastus codes with exactly same labels are
                // removed. The duplicates are found in case where a stop point has separate
                // records for both arrival and departure time.
                val hastusStopPointsAndTimingPlaces: List<Pair<String, String?>> =
                    filterOutConsecutiveDuplicates(
                        hastusTripStops.map { it.stopId to it.timingPlace }
                    )

                val hastusStopPointLabels: List<String> = hastusStopPointsAndTimingPlaces.map { it.first }
                val hastusPlaceLabels: List<String?> = hastusStopPointsAndTimingPlaces.map { it.second }

                val journeyPatternRefsMatchedByStopPointLabels: List<JoreJourneyPatternRef> =
                    journeyPatternRefsGroupedByRouteLabelAndDirection[hastusRouteLabelAndDirection]
                        .orEmpty() // won't really be empty because of previously done label-direction matching
                        .filter { journeyPatternRef ->
                            val joreStopPointLabels: List<String> = journeyPatternRef.stops.map { it.stopLabel }

                            joreStopPointLabels == hastusStopPointLabels
                        }

                if (journeyPatternRefsMatchedByStopPointLabels.isEmpty()) {
                    val exception = CannotFindJourneyPatternRefByStopPointLabelsException(
                        hastusRouteLabelAndDirection,
                        hastusStopPointLabels
                    )
                    LOGGER.warn(exception.message)
                    throw exception
                }

                val bestJourneyPatternRefMatch: JoreJourneyPatternRef =
                    journeyPatternRefsMatchedByStopPointLabels
                        .sortedByDescending {
                            // TODO Make sure that this is the appropriate ordering criteria when
                            //  finding JourneyPatternRef match.
                            it.snapshotTime
                        }
                        .firstOrNull { journeyPatternRef ->
                            val joreTimingPlaceLabels: List<String?> =
                                journeyPatternRef.stops.map { it.timingPlaceCode }

                            joreTimingPlaceLabels == hastusPlaceLabels
                        }
                        ?: run {
                            val exception = CannotFindJourneyPatternRefByTimingPlaceLabelsException(
                                hastusRouteLabelAndDirection,
                                hastusStopPointLabels,
                                hastusPlaceLabels
                            )
                            LOGGER.warn(exception.message)
                            throw exception
                        }

                results[hastusRouteLabelAndDirection] = bestJourneyPatternRefMatch
            }

            return results
        }

        private fun verifyRouteLabelsAndDirectionsAreMatched(
            hastusTrips: Set<RouteLabelAndDirection>,
            routesFromJourneyPatternRefs: Set<RouteLabelAndDirection>
        ) {
            val missingRouteLabelsAndDirections: List<RouteLabelAndDirection> = hastusTrips
                .subtract(routesFromJourneyPatternRefs)
                .sorted()

            if (missingRouteLabelsAndDirections.isNotEmpty()) {
                val exception = CannotFindJourneyPatternRefByRouteLabelAndDirectionException(
                    missingRouteLabelsAndDirections
                )
                LOGGER.warn(exception.message)
                throw exception
            }
        }
    }
}
