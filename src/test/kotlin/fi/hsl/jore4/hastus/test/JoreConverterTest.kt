package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JorePassingTime
import fi.hsl.jore4.hastus.data.jore.JoreStopPoint
import fi.hsl.jore4.hastus.data.mapper.JoreConverter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@DisplayName("Test the Jore converter")
class JoreConverterTest {

    @DisplayName("When converting parsed CSV to jore types")
    @Test
    fun convertParsedCsvTest() {
        val label = "Testing"

        val vehicleTypes = mapOf(VEHICLE_TYPE_1_KEY to VEHICLE_TYPE_1_ID, VEHICLE_TYPE_2_KEY to VEHICLE_TYPE_2_ID)
        val dayTypes = mapOf("MA" to MONDAY_DAY_TYPE_ID, "PE" to FRIDAY_DAY_TYPE_ID)

        val joreStopIds1 = listOf(
            JoreStopPoint(UUID.randomUUID(), "stop1", 0),
            JoreStopPoint(UUID.randomUUID(), "stop2", 1),
            JoreStopPoint(UUID.randomUUID(), "stop3", 2)
        )
        val joreJourneyPattern1 = JoreJourneyPattern("PATTERN1", UUID.randomUUID(), "stopping_bus_service", joreStopIds1)

        val joreStopIds2 = listOf(
            JoreStopPoint(UUID.randomUUID(), "stop21", 0),
            JoreStopPoint(UUID.randomUUID(), "stop22", 1),
            JoreStopPoint(UUID.randomUUID(), "stop23", 2)
        )
        val joreJourneyPattern2 = JoreJourneyPattern("PATTERN2", UUID.randomUUID(), "regional_bus_service", joreStopIds2)

        val joreJourneyPatterns = mapOf(
            joreJourneyPattern1.uniqueLabel!! to joreJourneyPattern1,
            joreJourneyPattern2.uniqueLabel!! to joreJourneyPattern2
        )

        val hastusData: List<IHastusData> = listOf(
            generateApplicationRecord(),
            generateBookingRecord("name", "booking", "description"),
            generateVehicleScheduleRecord(13),
            generateBlockRecord("block1", "service1", VEHICLE_TYPE_1_KEY),
            generateTripRecord("block1", "trip1", "PATTERN1"),
            generateTripStopRecord("trip1", "stop1", "0455", "T", ""),
            generateTripStopRecord("trip1", "stop2", "0510", "", "t"),
            generateTripStopRecord("trip1", "stop2", "0520", "", ""),
            generateTripStopRecord("trip1", "stop3", "0530", "T", ""),
            generateBlockRecord("block2", "service1", VEHICLE_TYPE_1_KEY),
            generateTripRecord("block2", "trip2", "PATTERN1"),
            generateTripStopRecord("trip2", "stop1", "0655", "T", ""),
            generateTripStopRecord("trip2", "stop2", "0710", "", ""),
            generateTripStopRecord("trip2", "stop2", "0720", "", "a"),
            generateTripStopRecord("trip2", "stop3", "0730", "T", ""),
            generateBlockRecord("block20", "service2", VEHICLE_TYPE_2_KEY),
            generateTripRecord("block20", "trip3", "PATTERN2"),
            generateTripStopRecord("trip3", "stop1", "0455", "T", ""),
            generateTripStopRecord("trip3", "stop2", "0520", "", ""),
            generateTripStopRecord("trip3", "stop3", "0530", "T", ""),
            generateBlockRecord("block21", "service2", VEHICLE_TYPE_2_KEY),
            generateTripRecord("block21", "trip4", "PATTERN2"),
            generateTripStopRecord("trip4", "stop1", "0655", "T", ""),
            generateTripStopRecord("trip4", "stop2", "0720", "", ""),
            generateTripStopRecord("trip4", "stop3", "0730", "T", "")
        )

        val converted = JoreConverter.convertHastusDataToJore(
            label,
            hastusData,
            joreJourneyPatterns,
            vehicleTypes,
            dayTypes
        )

        assertEquals(label, converted.label)
        assertEquals("name", converted.name)
        assertEquals("booking", converted.bookingLabel)
        assertEquals("description", converted.bookingDescription)

        assertEquals(2, converted.vehicleServices.size)

        val service1 = converted.vehicleServices[0]

        assertEquals(MONDAY_DAY_TYPE_ID, service1.dayType)
        assertEquals("service1", service1.name)

        assertEquals(2, service1.blocks.size)

        val block1 = service1.blocks[0]

        assertEquals(VEHICLE_TYPE_1_ID, block1.vehicleType)

        assertEquals(1, block1.vehicleJourneys.size)

        val journey1 = block1.vehicleJourneys[0]

        assertEquals(3, journey1.passingTimes.size)

        assertEquals(
            JorePassingTime(
                joreStopIds1[0].id,
                null,
                4.hours.plus(55.minutes)
            ),
            journey1.passingTimes[0]
        )
        assertEquals(
            JorePassingTime(
                joreStopIds1[1].id,
                5.hours.plus(10.minutes),
                5.hours.plus(20.minutes)
            ),
            journey1.passingTimes[1]
        )
        assertEquals(
            JorePassingTime(
                joreStopIds1[2].id,
                5.hours.plus(30.minutes),
                null
            ),
            journey1.passingTimes[2]
        )

        val block2 = service1.blocks[1]

        assertEquals(VEHICLE_TYPE_1_ID, block2.vehicleType)

        assertEquals(1, block2.vehicleJourneys.size)

        val journey2 = block2.vehicleJourneys[0]

        assertEquals(3, journey2.passingTimes.size)

        assertEquals(
            JorePassingTime(
                joreStopIds1[0].id,
                null,
                6.hours.plus(55.minutes)
            ),
            journey2.passingTimes[0]
        )
        assertEquals(
            JorePassingTime(
                joreStopIds1[1].id,
                7.hours.plus(10.minutes),
                7.hours.plus(20.minutes)
            ),
            journey2.passingTimes[1]
        )
        assertEquals(
            JorePassingTime(
                joreStopIds1[2].id,
                7.hours.plus(30.minutes),
                null
            ),
            journey2.passingTimes[2]
        )
    }

    companion object {

        private const val VEHICLE_TYPE_1_KEY = 10
        private val VEHICLE_TYPE_1_ID = UUID.randomUUID()

        private const val VEHICLE_TYPE_2_KEY = 20
        private val VEHICLE_TYPE_2_ID = UUID.randomUUID()

        private val MONDAY_DAY_TYPE_ID = UUID.randomUUID()
        private val FRIDAY_DAY_TYPE_ID = UUID.randomUUID()

        fun generateApplicationRecord(): ApplicationRecord {
            return ApplicationRecord(
                "HASTUS",
                "HSL",
                1.04,
                LocalDate.of(2019, 5, 2),
                LocalTime.of(21, 21, 57)
            )
        }

        fun generateBookingRecord(name: String, booking: String, description: String): BookingRecord {
            return BookingRecord(
                booking,
                description,
                name,
                5,
                LocalDate.of(2019, 8, 12),
                LocalDate.of(2020, 6, 14),
                "CONTRACT"
            )
        }

        fun generateVehicleScheduleRecord(dayType: Int): VehicleScheduleRecord {
            return VehicleScheduleRecord(
                "4571",
                dayType,
                0,
                "HSL",
                LocalDate.of(2019, 8, 12),
                LocalDate.of(2020, 6, 14),
                LocalDate.of(2019, 4, 30),
                LocalTime.of(11, 38, 55)
            )
        }

        fun generateBlockRecord(block: String, vehicleService: String, vehicleType: Int): BlockRecord {
            return BlockRecord(
                block,
                vehicleService,
                1,
                "4VARIS",
                "4VARIS",
                "4571",
                3,
                0,
                vehicleType
            )
        }

        fun generateTripRecord(block: String, trip: String, relation: String): TripRecord {
            return TripRecord(
                "CONTRACT",
                block,
                trip,
                "1595",
                0,
                relation,
                "4571",
                "2",
                "05:04",
                "05:55",
                60,
                0,
                4,
                25.200,
                "0",
                "",
                1,
                2,
                isVehicleTypeMandatory = false,
                isBackupTrip = false,
                isExtraTrip = false
            )
        }

        fun generateTripStopRecord(trip: String, stop: String, time: String, type: String, note: String): TripStopRecord {
            return TripStopRecord(
                trip,
                "PLACE",
                stop,
                "",
                "",
                "",
                "",
                time,
                0.0,
                type,
                note
            )
        }
    }
}
