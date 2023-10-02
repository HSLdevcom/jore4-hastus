package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.JoreJourneyType
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import fi.hsl.jore4.hastus.data.jore.JoreBlock
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JorePassingTime
import fi.hsl.jore4.hastus.data.jore.JoreStopPoint
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
        hastusData: List<IHastusData>,
        vehicleTypeIndex: Map<Int, UUID>,
        dayTypeIndex: Map<String, UUID>,
        journeyPatternsIndexedByRouteLabel: Map<String, JoreJourneyPattern>
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
        val vehicleServiceNames: List<String> = hastusBlockIndex
            .keys
            .map { it.vehicleServiceName }
            .distinct()

        val dayTypeId: UUID = determineIdOfDayType(
            hastusBookingRecord.startDate,
            hastusBookingRecord.endDate,
            hastusVehicleScheduleRecord.scheduleType,
            dayTypeIndex
        )

        val vehicleServices: List<JoreVehicleService> = vehicleServiceNames.map { vehicleServiceName ->
            mapToJoreVehicleService(
                vehicleServiceName,
                dayTypeId,
                hastusBlockIndex.filter { block -> block.key.vehicleServiceName == vehicleServiceName },
                journeyPatternsIndexedByRouteLabel,
                vehicleTypeIndex
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
        val key = if (hastusBookingRecordStartDate == hastusBookingRecordEndDate) {
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
        dayTypeId: UUID,
        hastusBlockIndex: Map<BlockRecord, Map<TripRecord, List<TripStopRecord>>>,
        journeyPatternsIndexedByRouteLabel: Map<String, JoreJourneyPattern>,
        vehicleTypeIndex: Map<Int, UUID>
    ): JoreVehicleService {
        return JoreVehicleService(
            vehicleServiceName,
            dayTypeId,
            hastusBlockIndex.map {
                mapToJoreBlock(
                    it.key,
                    it.value,
                    journeyPatternsIndexedByRouteLabel,
                    vehicleTypeIndex
                )
            }
        )
    }

    private fun mapToJoreBlock(
        hastusBlock: BlockRecord,
        hastusTripIndex: Map<TripRecord, List<TripStopRecord>>,
        journeyPatternsIndexedByRouteLabel: Map<String, JoreJourneyPattern>,
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
            hastusTripIndex.map { mapToJoreVehicleJourney(it.key, it.value, journeyPatternsIndexedByRouteLabel) }
        )
    }

    private fun mapToJoreVehicleJourney(
        hastusTrip: TripRecord,
        hastusStops: List<TripStopRecord>,
        journeyPatternsIndexedByRouteLabel: Map<String, JoreJourneyPattern>
    ): JoreVehicleJourney {
        val routeLabel: String = hastusTrip.tripRoute
        val journeyPatternStopLabels = hastusStops.map { it.stopId }.distinct()
        val journeyPatternStopsIndexedByLabel: Map<String, JoreStopPoint> =
            journeyPatternsIndexedByRouteLabel[routeLabel]?.stops?.associate { it.label to it }.orEmpty()

        if (!journeyPatternStopsIndexedByLabel.keys.containsAll(journeyPatternStopLabels)) {
            val unknowns = journeyPatternStopLabels.subtract(journeyPatternStopsIndexedByLabel.keys)
            LOGGER.error { "Trip $hastusTrip has unknown stop along the route: $unknowns" }
            throw IllegalStateException("Trip $routeLabel contains unknown stop along the route: $unknowns")
        }

        val journeyPatternId = journeyPatternsIndexedByRouteLabel[routeLabel]?.journeyPatternId!!

        return JoreVehicleJourney(
            hastusTrip.tripNumber,
            hastusTrip.turnaroundTime.minutes,
            hastusTrip.layoverTime.minutes,
            mapJourneyType(hastusTrip.tripType),
            hastusTrip.tripDisplayedName,
            hastusTrip.isVehicleTypeMandatory,
            hastusTrip.isBackupTrip,
            hastusTrip.isExtraTrip,
            journeyPatternId,
            journeyPatternStopLabels
                .mapIndexed { index, stopLabel ->
                    mapToJorePassingTimes(
                        hastusStops.filter { hastusStop -> hastusStop.stopId == stopLabel },
                        journeyPatternStopsIndexedByLabel[stopLabel]!!.id,
                        index == 0,
                        index == journeyPatternStopLabels.size - 1
                    )
                }
        )
    }

    private fun mapJourneyType(type: Int) = when (type) {
        1, 2 -> JoreJourneyType.SERVICE_JOURNEY
        3 -> JoreJourneyType.DRY_RUN
        else -> JoreJourneyType.STANDARD
    }

    private fun mapToJorePassingTimes(
        hastusStop: List<TripStopRecord>,
        stopReferenceId: UUID,
        isFirstStop: Boolean,
        isLastStop: Boolean
    ): JorePassingTime {
        val passingDefinition = getTime(hastusStop.firstOrNull { it.note == "" }?.passingTime)

        val arrivalDefinition = if (isFirstStop) {
            null
        } else {
            getTime(hastusStop.firstOrNull { it.note == "t" }?.passingTime)
                ?: passingDefinition // Stops with note 't' are arrival times
        }

        val departureDefinition = if (isLastStop) {
            null
        } else {
            getTime(hastusStop.firstOrNull { it.note == "a" }?.passingTime)
                ?: passingDefinition // Stops with note 'a' are departure times
        }

        return JorePassingTime(
            stopReferenceId,
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
}