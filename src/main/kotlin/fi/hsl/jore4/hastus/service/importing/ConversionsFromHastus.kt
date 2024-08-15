package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.JoreJourneyType
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.data.hastus.imp.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.imp.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.imp.ImportableItem
import fi.hsl.jore4.hastus.data.hastus.imp.TripRecord
import fi.hsl.jore4.hastus.data.hastus.imp.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.imp.VehicleScheduleRecord
import fi.hsl.jore4.hastus.data.jore.JoreBlock
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternRef
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternStopRef
import fi.hsl.jore4.hastus.data.jore.JorePassingTime
import fi.hsl.jore4.hastus.data.jore.JoreVehicleJourney
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.data.jore.JoreVehicleService
import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object ConversionsFromHastus {
    private val LOGGER = KotlinLogging.logger {}

    fun convertHastusDataToJore(
        hastusData: List<ImportableItem>,
        vehicleTypeIndex: Map<Int, UUID>,
        dayTypeIndex: Map<String, UUID>,
        journeyPatternRefIndex: Map<RouteLabelAndDirection, JoreJourneyPatternRef>
    ): JoreVehicleScheduleFrame {
        val hastusBookingRecord: BookingRecord = hastusData.filterIsInstance<BookingRecord>().first()
        val hastusVehicleScheduleRecord: VehicleScheduleRecord =
            hastusData.filterIsInstance<VehicleScheduleRecord>().first()
        val hastusBlockRecords: List<BlockRecord> = hastusData.filterIsInstance<BlockRecord>()
        val hastusTripRecords: List<TripRecord> = hastusData.filterIsInstance<TripRecord>()
        val hastusTripStopRecords: List<TripStopRecord> = hastusData.filterIsInstance<TripStopRecord>()

        // Construct relations according to the keys in the elements.
        val hastusTripIndex: Map<TripRecord, List<TripStopRecord>> =
            hastusTripRecords.associateWith { trip ->
                hastusTripStopRecords.filter { stop -> stop.tripInternalNumber == trip.tripInternalNumber }
            }
        val hastusBlockIndex: Map<BlockRecord, Map<TripRecord, List<TripStopRecord>>> =
            hastusBlockRecords.associateWith { block ->
                hastusTripIndex.filter { trip -> trip.key.blockNumber == block.internalNumber }
            }

        // Collect the names for all included vehicle services.
        val vehicleServiceNames: List<String> =
            hastusBlockIndex
                .keys
                .map { it.vehicleServiceName }
                .distinct()

        val dayTypeId: UUID =
            determineIdOfDayType(
                hastusBookingRecord.startDate,
                hastusBookingRecord.endDate,
                hastusVehicleScheduleRecord.scheduleType,
                dayTypeIndex
            )

        val vehicleServices: List<JoreVehicleService> =
            vehicleServiceNames.map { vehicleServiceName ->
                mapToJoreVehicleService(
                    vehicleServiceName,
                    vehicleTypeIndex,
                    dayTypeId,
                    hastusBlockIndex.filter { block -> block.key.vehicleServiceName == vehicleServiceName },
                    journeyPatternRefIndex
                )
            }

        return JoreVehicleScheduleFrame(
            hastusBookingRecord.name,
            hastusBookingRecord.name,
            hastusBookingRecord.booking,
            hastusBookingRecord.bookingDescription,
            hastusBookingRecord.startDate,
            hastusBookingRecord.endDate,
            vehicleServices
        )
    }

    // Convert predefined magic numbers into those used in the database.
    private fun determineIdOfDayType(
        hastusBookingRecordStartDate: LocalDate,
        hastusBookingRecordEndDate: LocalDate,
        day: Int,
        dayTypeIndex: Map<String, UUID>
    ): UUID {
        val key =
            if (hastusBookingRecordStartDate == hastusBookingRecordEndDate) {
                when (hastusBookingRecordStartDate.dayOfWeek) {
                    DayOfWeek.MONDAY -> "MA"
                    DayOfWeek.TUESDAY -> "TI"
                    DayOfWeek.WEDNESDAY -> "KE"
                    DayOfWeek.THURSDAY -> "TO"
                    DayOfWeek.FRIDAY -> "PE"
                    DayOfWeek.SATURDAY -> "LA"
                    DayOfWeek.SUNDAY -> "SU"
                    else -> throw IllegalStateException("StartDate had a null day of week")
                }
            } else {
                when (day) {
                    0 -> "MP"
                    25 -> "MT"
                    13 -> "MA"
                    14 -> "TI"
                    11 -> "KE"
                    3 -> "TO"
                    4 -> "PE"
                    5 -> "LA"
                    6 -> "SU"
                    else -> ""
                }
            }

        if (!dayTypeIndex.containsKey(key)) {
            throw IllegalArgumentException("Unknown schedule type $day when converting to Jore day type")
        }

        return dayTypeIndex[key]!!
    }

    private fun mapToJoreVehicleService(
        vehicleServiceName: String,
        vehicleTypeIndex: Map<Int, UUID>,
        dayTypeId: UUID,
        hastusBlockIndex: Map<BlockRecord, Map<TripRecord, List<TripStopRecord>>>,
        journeyPatternRefIndex: Map<RouteLabelAndDirection, JoreJourneyPatternRef>
    ): JoreVehicleService {
        return JoreVehicleService(
            vehicleServiceName,
            dayTypeId,
            hastusBlockIndex.map {
                mapToJoreBlock(
                    it.key,
                    it.value,
                    journeyPatternRefIndex,
                    vehicleTypeIndex
                )
            }
        )
    }

    private fun mapToJoreBlock(
        hastusBlock: BlockRecord,
        hastusTripIndex: Map<TripRecord, List<TripStopRecord>>,
        journeyPatternRefIndex: Map<RouteLabelAndDirection, JoreJourneyPatternRef>,
        vehicleTypeIndex: Map<Int, UUID>
    ): JoreBlock {
        if (!vehicleTypeIndex.containsKey(hastusBlock.vehicleType)) {
            throw IllegalArgumentException("Unknown vehicle type ${hastusBlock.vehicleType} when converting to Jore vehicle type")
        }

        return JoreBlock(
            hastusBlock.internalNumber,
            hastusBlock.prepOutTime.minutes,
            hastusBlock.prepInTime.minutes,
            vehicleTypeIndex[hastusBlock.vehicleType]!!,
            hastusTripIndex.map { mapToJoreVehicleJourney(it.key, it.value, journeyPatternRefIndex) }
        )
    }

    private fun mapToJoreVehicleJourney(
        hastusTrip: TripRecord,
        hastusStops: List<TripStopRecord>,
        journeyPatternRefIndex: Map<RouteLabelAndDirection, JoreJourneyPatternRef>
    ): JoreVehicleJourney {
        val routeLabelAndDirection: RouteLabelAndDirection = extractRouteLabelAndDirection(hastusTrip)
        val hastusStopLabels = hastusStops.map { it.stopId }.distinct()

        val journeyPatternRef: JoreJourneyPatternRef =
            journeyPatternRefIndex[routeLabelAndDirection]
                ?: run {
                    // Should never happen during application runtime because journeyPatternRefIndex is
                    // expected to be complete at this point. Possible failures should have occurred
                    // earlier in the processing chain. Hence, logging as an error.
                    val exception =
                        CannotFindJourneyPatternRefByRouteLabelAndDirectionException(listOf(routeLabelAndDirection))
                    LOGGER.error(exception.message)
                    throw exception
                }

        val journeyPatternStopRefsIndexedByLabel: Map<String, JoreJourneyPatternStopRef> =
            journeyPatternRef
                .stops
                .associateBy { it.stopLabel }
                .also { resultMap: Map<String, JoreJourneyPatternStopRef> ->

                    val stopLabelsExtractedFromJourneyPatternStopRefs: Set<String> = resultMap.keys

                    if (!stopLabelsExtractedFromJourneyPatternStopRefs.containsAll(hastusStopLabels)) {
                        // Should never happen during application runtime because journeyPatternStopRefs
                        // are expected to be complete at this point. The failure should have occurred
                        // in the earlier stages of the processing chain. Hence, logging as an error.
                        val unknownStopLabels = hastusStopLabels.subtract(stopLabelsExtractedFromJourneyPatternStopRefs)
                        val errorMessage =
                            "Hastus trip '$routeLabelAndDirection' contains unknown stop points along the route: ${
                                unknownStopLabels.joinToString(prefix = "'", separator = ",", postfix = "'")
                            }"
                        LOGGER.error(errorMessage)
                        throw ErrorWhileProcessingHastusDataException(errorMessage)
                    }
                }

        val journeyPatternRefId = journeyPatternRef.journeyPatternRefId

        return JoreVehicleJourney(
            hastusTrip.tripNumber,
            hastusTrip.contract,
            hastusTrip.turnaroundTime.minutes,
            hastusTrip.layoverTime.minutes,
            mapJourneyType(hastusTrip.tripType),
            hastusTrip.tripDisplayedName,
            hastusTrip.isVehicleTypeMandatory,
            hastusTrip.isBackupTrip,
            hastusTrip.isExtraTrip,
            journeyPatternRefId,
            hastusStopLabels
                .mapIndexed { index, stopLabel ->
                    mapToJorePassingTimes(
                        hastusStops.filter { hastusStop -> hastusStop.stopId == stopLabel },
                        journeyPatternStopRefsIndexedByLabel[stopLabel]!!.id,
                        index == 0,
                        index == hastusStopLabels.size - 1
                    )
                }
        )
    }

    private fun mapJourneyType(type: Int) =
        when (type) {
            1, 2 -> JoreJourneyType.SERVICE_JOURNEY
            3 -> JoreJourneyType.DRY_RUN
            else -> JoreJourneyType.STANDARD
        }

    private fun mapToJorePassingTimes(
        hastusStop: List<TripStopRecord>,
        journeyPatternStopRefId: UUID,
        isFirstStop: Boolean,
        isLastStop: Boolean
    ): JorePassingTime {
        val passingDefinition = getTime(hastusStop.firstOrNull { it.note == "" }?.passingTime)

        val arrivalDefinition =
            if (isFirstStop) {
                null
            } else {
                getTime(hastusStop.firstOrNull { it.note == "t" }?.passingTime)
                    ?: passingDefinition // Stops with note 't' are arrival times
            }

        val departureDefinition =
            if (isLastStop) {
                null
            } else {
                getTime(hastusStop.firstOrNull { it.note == "a" }?.passingTime)
                    ?: passingDefinition // Stops with note 'a' are departure times
            }

        return JorePassingTime(
            journeyPatternStopRefId,
            arrivalDefinition,
            departureDefinition
        )
    }

    // Convert a HH:MM string into two possibly >24h numbers.
    private fun getTime(time: String?): Duration? {
        val hours = time?.substring(0, 2)?.toInt()?.hours
        val minutes = time?.substring(2, 4)?.toInt()?.minutes

        if (hours != null && minutes != null) {
            return hours.plus(minutes)
        }
        return null
    }

    /**
     * Extracts Jore4 unique route label containing variant info from Hastus trip record.
     *
     * In Hastus trip record, the label is split between two fields:
     * (1) tripRoute, which is basically the same as the Jore4 line number
     * (2) variant, which can be empty or contain both letters and numbers
     *
     * The current specs are:
     * (i) the variant part is appended directly to the line number (the tripRoute field) except
     * for the last character, if it is a number. If the last character is a number, it is
     * separated by an underscore ('_').
     * (ii) if the variant part ends with a '1' or '2' the last character of the variant is
     * omitted.
     */
    fun extractRouteLabel(trip: TripRecord): String {
        val lineLabel = trip.tripRoute
        val variant = trip.variant

        if (variant.isEmpty() || variant == "1" || variant == "2") {
            // plain line label
            return lineLabel
        }

        val lastChar: Char = variant.last()

        val trimmedVariant: String =
            if (lastChar.isDigit()) {
                // last char stripped away
                val head: String = variant.substring(0, variant.length - 1)

                if (lastChar == '1' || lastChar == '2') {
                    head
                } else {
                    "${head}_$lastChar"
                }
            } else {
                variant
            }

        return "$lineLabel$trimmedVariant"
    }

    fun extractRouteDirection(trip: TripRecord): JoreRouteDirection {
        return when (trip.direction) {
            1 -> JoreRouteDirection.OUTBOUND
            2 -> JoreRouteDirection.INBOUND
            null -> throw IllegalArgumentException("Null route direction encountered (dead run)")
            else -> throw IllegalArgumentException("Unknown Hastus route direction: ${trip.direction}")
        }
    }

    fun extractRouteLabelAndDirection(trip: TripRecord) =
        RouteLabelAndDirection(
            extractRouteLabel(trip),
            extractRouteDirection(trip)
        )
}
