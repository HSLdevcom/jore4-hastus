package fi.hsl.jore4.hastus.data.mapper

import fi.hsl.jore4.hastus.data.format.JoreJourneyType
import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import fi.hsl.jore4.hastus.data.jore.JoreBlock
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JorePassingTime
import fi.hsl.jore4.hastus.data.jore.JoreVehicleJourney
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.data.jore.JoreVehicleService
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class JoreConverter {

    companion object {
        fun convertHastusDataToJore(
            name: String,
            hastusData: List<IHastusData>,
            joreJourneyPatterns: Map<String, JoreJourneyPattern>,
            vehicleTypes: Map<Int, UUID>,
            dayTypes: Map<String, UUID>
        ): JoreVehicleScheduleFrame {
            val applicationRecord: ApplicationRecord = hastusData.filterIsInstance<ApplicationRecord>().first()
            val bookingRecord: BookingRecord = hastusData.filterIsInstance<BookingRecord>().first()
            val vehicleScheduleRecord: VehicleScheduleRecord = hastusData.filterIsInstance<VehicleScheduleRecord>().first()
            val blockRecords: List<BlockRecord> = hastusData.filterIsInstance<BlockRecord>()
            val tripRecords: List<TripRecord> = hastusData.filterIsInstance<TripRecord>()
            val tripStopRecords: List<TripStopRecord> = hastusData.filterIsInstance<TripStopRecord>()

            // Construct relations according to the keys in the elements
            val tripMap =
                tripRecords.associateWith { trip -> tripStopRecords.filter { stop -> stop.tripInternalNumber == trip.tripInternalNumber } }
            val blockMap =
                blockRecords.associateWith { block -> tripMap.filter { trip -> trip.key.blockNumber == block.internalNumber } }

            // Collect the names for all included vehicle services
            val vehicleServiceLabels: List<String> = blockMap.keys.map { it.vehicleServiceName }.distinct()

            val vehicleServices: List<JoreVehicleService> = vehicleServiceLabels.map { vehicleService ->
                mapToJoreVehicleService(
                    vehicleService,
                    blockMap.filter { block -> block.key.vehicleServiceName == vehicleService },
                    joreJourneyPatterns,
                    vehicleTypes,
                    determineDayType(
                        bookingRecord.startDate,
                        bookingRecord.endDate,
                        vehicleScheduleRecord.scheduleType,
                        dayTypes
                    )
                )
            }
            LocalDate.now().dayOfWeek
            return JoreVehicleScheduleFrame(
                bookingRecord.name,
                name,
                bookingRecord.booking,
                bookingRecord.bookingDescription,
                bookingRecord.startDate,
                bookingRecord.endDate,
                vehicleServices
            )
        }

        // Convert predefined magic numbers into those used in the database
        private fun determineDayType(
            startDate: LocalDate,
            endDate: LocalDate,
            day: Int,
            dayTypes: Map<String, UUID>
        ): UUID {
            val key = if (startDate == endDate) {
                when (startDate.dayOfWeek) {
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
            if (!dayTypes.containsKey(key)) {
                throw IllegalArgumentException("Unknown schedule type $day when converting to Jore day type")
            }
            return dayTypes[key]!!
        }

        private fun mapToJoreVehicleService(
            name: String,
            blocks: Map<BlockRecord, Map<TripRecord, List<TripStopRecord>>>,
            joreJourneyPatterns: Map<String, JoreJourneyPattern>,
            vehicleTypes: Map<Int, UUID>,
            dayType: UUID
        ): JoreVehicleService {
            return JoreVehicleService(
                name,
                dayType,
                blocks.map { mapToJoreBlock(it.key, it.value, joreJourneyPatterns, vehicleTypes) }
            )
        }

        private fun mapToJoreBlock(
            block: BlockRecord,
            trips: Map<TripRecord, List<TripStopRecord>>,
            joreJourneyPatterns: Map<String, JoreJourneyPattern>,
            vehicleTypes: Map<Int, UUID>
        ): JoreBlock {
            if (!vehicleTypes.containsKey(block.vehicleType)) {
                throw IllegalArgumentException("Unknown vehicle type ${block.vehicleType} when converting to Jore vehicle type")
            }

            return JoreBlock(
                block.internalNumber,
                block.prepOutTime.minutes,
                block.prepInTime.minutes,
                vehicleTypes[block.vehicleType]!!,
                trips.map { mapToJoreVehicleJourney(it.key, it.value, joreJourneyPatterns) }
            )
        }

        private fun mapToJoreVehicleJourney(
            trip: TripRecord,
            stops: List<TripStopRecord>,
            joreJourneyPatterns: Map<String, JoreJourneyPattern>
        ): JoreVehicleJourney {
            val stopIds = stops.map { it.stopId }.distinct()
            val joreStopsIdsOnRoute = joreJourneyPatterns[trip.tripRoute]?.stops.orEmpty()
            val journeyPatternId = joreJourneyPatterns[trip.tripRoute]?.journeyPatternId!!
            return JoreVehicleJourney(
                trip.tripNumber,
                trip.turnaroundTime.minutes,
                trip.layoverTime.minutes,
                mapJourneyType(trip.tripType),
                trip.tripDisplayedName,
                trip.isVehicleTypeMandatory,
                trip.isBackupTrip,
                trip.isExtraTrip,
                journeyPatternId,
                stopIds.mapIndexed { index, stopId ->
                    mapToJorePassingTimes(
                        stops.filter { stop -> stop.stopId == stopId },
                        joreStopsIdsOnRoute[index].id,
                        index == 0,
                        index == stopIds.size - 1
                    )
                }
            )
        }

        private fun mapJourneyType(type: Int): JoreJourneyType {
            return when (type) {
                1 -> JoreJourneyType.SERVICE_JOURNEY
                2 -> JoreJourneyType.SERVICE_JOURNEY
                3 -> JoreJourneyType.DRY_RUN
                else -> JoreJourneyType.STANDARD
            }
        }

        private fun mapToJorePassingTimes(
            stop: List<TripStopRecord>,
            stopReferenceId: UUID,
            isFirstStop: Boolean,
            isLastStop: Boolean
        ): JorePassingTime {
            val passingDefinition = getTime(stop.firstOrNull { it.note == "" }?.passingTime)

            val arrivalDefinition = if (isFirstStop) {
                null
            } else {
                getTime(stop.firstOrNull { it.note == "t" }?.passingTime) ?: passingDefinition // Stops with note 't' are arrival times
            }
            val departureDefinition = if (isLastStop) {
                null
            } else {
                getTime(stop.firstOrNull { it.note == "a" }?.passingTime) ?: passingDefinition // Stops with note 'a' are departure times
            }

            return JorePassingTime(
                stopReferenceId,
                arrivalDefinition,
                departureDefinition
            )
        }

        // Convert a HH:MM string into two possibly >24h numbers
        private fun getTime(time: String?): Duration? {
            val hours = time?.substring(0, 2)?.toInt()?.hours
            val minutes = time?.substring(2, 4)?.toInt()?.minutes

            if (hours != null && minutes != null) {
                return hours.plus(minutes)
            }
            return null
        }
    }
}
