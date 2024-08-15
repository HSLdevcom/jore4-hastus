package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.data.hastus.imp.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.imp.ImportableItem
import fi.hsl.jore4.hastus.data.hastus.imp.TripRecord
import fi.hsl.jore4.hastus.data.hastus.imp.TripStopRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternRef
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.graphql.GraphQLServiceFactory
import fi.hsl.jore4.hastus.util.CollectionUtil.filterOutConsecutiveDuplicates
import fi.hsl.jore4.hastus.util.CsvReader
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

private val LOGGER = KotlinLogging.logger {}

@Service
class ImportService(
    private val graphQLServiceFactory: GraphQLServiceFactory
) {
    fun importTimetablesFromCsv(
        csv: String,
        hasuraHeaders: Map<String, String>
    ): UUID? {
        val hastusItems: List<ImportableItem> = getHastusDataItems(csv)
        val graphQLService: GraphQLService = graphQLServiceFactory.createForSession(hasuraHeaders)

        val hastusBookingRecord: BookingRecord = hastusItems.filterIsInstance<BookingRecord>().first()

        val hastusTrips: List<TripRecord> = hastusItems.filterIsInstance<TripRecord>()
        val hastusTripStops: List<TripStopRecord> = hastusItems.filterIsInstance<TripStopRecord>()

        val uniqueRouteLabels: List<String> =
            hastusTrips
                .map(ConversionsFromHastus::extractRouteLabel)
                .distinct() // TODO: is distinct operation really required?

        val journeyPatternRefs: List<JoreJourneyPatternRef> =
            graphQLService.getJourneyPatternReferences(
                uniqueRouteLabels,
                hastusBookingRecord.startDate
            )
        LOGGER.debug { "Fetched journey pattern references: $journeyPatternRefs" }

        val vehicleTypeIndex: Map<Int, UUID> = graphQLService.getVehicleTypes()
        LOGGER.debug { "Using vehicle types: $vehicleTypeIndex" }

        val dayTypeIndex: Map<String, UUID> = graphQLService.getDayTypes()
        LOGGER.debug { "Using day types: $dayTypeIndex" }

        val journeyPatternRefIndex: Map<RouteLabelAndDirection, JoreJourneyPatternRef> =
            findMatchingJourneyPatternRefForEachHastusTrip(journeyPatternRefs, hastusTrips, hastusTripStops)

        val vehicleScheduleFrame: JoreVehicleScheduleFrame =
            ConversionsFromHastus.convertHastusDataToJore(
                hastusItems,
                vehicleTypeIndex,
                dayTypeIndex,
                journeyPatternRefIndex
            )

        val selectedJourneyPatternRefs: Collection<JoreJourneyPatternRef> = journeyPatternRefIndex.values

        return graphQLService.persistVehicleScheduleFrame(vehicleScheduleFrame, selectedJourneyPatternRefs)
    }

    private fun getHastusDataItems(csv: String): List<ImportableItem> = filterOutDeadRunItems(READER.parseCsv(csv))

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
                        hastusTripStops.map {
                            // By using effectiveTimingPlace, it is taken into account whether the
                            // stop point is used as a timing point in journey pattern.
                            it.stopId to it.effectiveTimingPlace
                        }
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
                    val exception =
                        CannotFindJourneyPatternRefByStopPointLabelsException(
                            hastusRouteLabelAndDirection,
                            hastusStopPointLabels
                        )
                    LOGGER.warn(exception.reason)
                    throw exception
                }

                val journeyPatternRefsMatchedByTimingPlaceLabels: List<JoreJourneyPatternRef> =
                    journeyPatternRefsMatchedByStopPointLabels.filter { journeyPatternRef ->
                        val joreTimingPlaceLabels: List<String?> =
                            journeyPatternRef.stops.map { it.timingPlaceCode }

                        joreTimingPlaceLabels == hastusPlaceLabels
                    }

                if (journeyPatternRefsMatchedByTimingPlaceLabels.isEmpty()) {
                    val exception =
                        CannotFindJourneyPatternRefByTimingPlaceLabelsException(
                            hastusRouteLabelAndDirection,
                            hastusStopPointLabels,
                            hastusPlaceLabels
                        )
                    LOGGER.warn(exception.reason)
                    throw exception
                }

                val bestJourneyPatternRefMatch: JoreJourneyPatternRef =
                    journeyPatternRefsMatchedByTimingPlaceLabels
                        .sortedWith(
                            compareByDescending<JoreJourneyPatternRef> {
                                // By choosing the one with the latest validity start date, we are
                                // effectively choosing the route/journey-pattern that is currently
                                // active or was most recently active among the candidates.
                                it.routeValidityStart
                            }.thenByDescending {
                                // The last exported item with the most up-to-date route information is
                                // picked.
                                it.snapshotTime
                            }
                        ).first()

                results[hastusRouteLabelAndDirection] = bestJourneyPatternRefMatch
            }

            return results
        }

        private fun verifyRouteLabelsAndDirectionsAreMatched(
            hastusTrips: Set<RouteLabelAndDirection>,
            routesFromJourneyPatternRefs: Set<RouteLabelAndDirection>
        ) {
            val missingRouteLabelsAndDirections: List<RouteLabelAndDirection> =
                hastusTrips
                    .subtract(routesFromJourneyPatternRefs)
                    .sorted()

            if (missingRouteLabelsAndDirections.isNotEmpty()) {
                val exception =
                    CannotFindJourneyPatternRefByRouteLabelAndDirectionException(
                        missingRouteLabelsAndDirections
                    )
                LOGGER.warn(exception.reason)
                throw exception
            }
        }

        private fun filterOutDeadRunItems(hastusItems: List<ImportableItem>): List<ImportableItem> {
            val internalNumbersOfDeadRuns: List<String> =
                hastusItems
                    .filterIsInstance<TripRecord>()
                    .filter { trip ->
                        // Null direction denotes a dead run which is not imported to Jore4.
                        trip.direction == null
                    }.map {
                        it.tripInternalNumber.also {
                            LOGGER.info {
                                "Found a dead run trip with internal number $it. " +
                                    "Discarding the trip and the related stop points."
                            }
                        }
                    }

            return hastusItems
                .filter { item ->
                    when (item) {
                        is TripRecord -> item.tripInternalNumber !in internalNumbersOfDeadRuns
                        is TripStopRecord -> item.tripInternalNumber !in internalNumbersOfDeadRuns
                        else -> true
                    }
                }
        }
    }
}
