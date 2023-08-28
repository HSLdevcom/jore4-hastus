package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternRef
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternStopRef
import fi.hsl.jore4.hastus.data.jore.JorePassingTime
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.util.DateTimeUtil
import fi.hsl.jore4.hastus.util.DateTimeUtil.toOffsetDateTimeAtDefaultZone
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@DisplayName("Test type conversions from Hastus to Jore")
class ConversionsFromHastusTest {

    @DisplayName("Test method: convertHastusDataToJore")
    @Nested
    inner class TestConvertHastusDataToJore {

        @Test
        fun `when converting parsed CSV to Jore object hierarchy`() {
            val hastusBookingRecordName = "Testing"

            val vehicleTypeIndex = mapOf(
                VEHICLE_TYPE_1_KEY to VEHICLE_TYPE_1_ID,
                VEHICLE_TYPE_2_KEY to VEHICLE_TYPE_2_ID
            )
            val dayTypeIndex = mapOf(
                "MA" to MONDAY_DAY_TYPE_ID,
                "PE" to FRIDAY_DAY_TYPE_ID
            )

            val routeValidityStartDate = LocalDate.of(2023, 1, 1)
            val observationTime: OffsetDateTime = routeValidityStartDate
                .plusDays(1)
                .toOffsetDateTimeAtDefaultZone()

            val stopRefList1 = listOf(
                JoreJourneyPatternStopRef(UUID.randomUUID(), 0, "stop1", "TP1"),
                JoreJourneyPatternStopRef(UUID.randomUUID(), 1, "stop2", null),
                JoreJourneyPatternStopRef(UUID.randomUUID(), 2, "stop3", "TP3")
            )
            val journeyPatternRef1 = JoreJourneyPatternRef(
                journeyPatternRefId = UUID.randomUUID(),
                journeyPatternId = UUID.randomUUID(),
                routeUniqueLabel = "ROUTE-1",
                routeDirection = JoreRouteDirection.OUTBOUND,
                routeValidityStart = routeValidityStartDate,
                routeValidityEnd = LocalDate.of(2050, 12, 31),
                typeOfLine = "stopping_bus_service",
                snapshotTime = DateTimeUtil.currentDateTimeAtDefaultZone(),
                observationTime = observationTime,
                stops = stopRefList1
            )

            val stopRefList2 = listOf(
                JoreJourneyPatternStopRef(UUID.randomUUID(), 0, "stop21", "TP21"),
                JoreJourneyPatternStopRef(UUID.randomUUID(), 1, "stop22", null),
                JoreJourneyPatternStopRef(UUID.randomUUID(), 2, "stop23", "TP23")
            )
            val journeyPatternRef2 = JoreJourneyPatternRef(
                journeyPatternRefId = UUID.randomUUID(),
                journeyPatternId = UUID.randomUUID(),
                routeUniqueLabel = "ROUTE-2",
                routeDirection = JoreRouteDirection.INBOUND,
                routeValidityStart = routeValidityStartDate,
                routeValidityEnd = LocalDate.of(2050, 12, 31),
                typeOfLine = "stopping_bus_service",
                snapshotTime = DateTimeUtil.currentDateTimeAtDefaultZone(),
                observationTime = observationTime,
                stops = stopRefList2
            )

            val journeyPatternRefsIndexedByRouteId = mapOf(
                journeyPatternRef1.routeLabelAndDirection to journeyPatternRef1,
                journeyPatternRef2.routeLabelAndDirection to journeyPatternRef2
            )

            val hastusData: List<IHastusData> = listOf(
                generateApplicationRecord(),
                generateBookingRecord(hastusBookingRecordName, "booking", "description"),
                generateVehicleScheduleRecord(13),
                generateBlockRecord("block1", "service1", VEHICLE_TYPE_1_KEY),
                generateTripRecord("block1", "trip1", "ROUTE-1", "1", direction = 1),
                generateTripStopRecord("trip1", "stop1", "0455", "T", ""),
                generateTripStopRecord("trip1", "stop2", "0510", "", "t"),
                generateTripStopRecord("trip1", "stop2", "0520", "", ""),
                generateTripStopRecord("trip1", "stop3", "0530", "T", ""),
                generateBlockRecord("block2", "service1", VEHICLE_TYPE_1_KEY),
                generateTripRecord("block2", "trip2", "ROUTE-1", "1", direction = 1),
                generateTripStopRecord("trip2", "stop1", "0655", "T", ""),
                generateTripStopRecord("trip2", "stop2", "0710", "", ""),
                generateTripStopRecord("trip2", "stop2", "0720", "", "a"),
                generateTripStopRecord("trip2", "stop3", "0730", "T", ""),
                generateBlockRecord("block20", "service2", VEHICLE_TYPE_2_KEY),
                generateTripRecord("block20", "trip3", "ROUTE-2", "2", direction = 2),
                generateTripStopRecord("trip3", "stop21", "0455", "T", ""),
                generateTripStopRecord("trip3", "stop22", "0520", "", ""),
                generateTripStopRecord("trip3", "stop23", "0530", "T", ""),
                generateBlockRecord("block21", "service2", VEHICLE_TYPE_2_KEY),
                generateTripRecord("block21", "trip4", "ROUTE-2", "2", direction = 2),
                generateTripStopRecord("trip4", "stop21", "0655", "T", ""),
                generateTripStopRecord("trip4", "stop22", "0720", "", ""),
                generateTripStopRecord("trip4", "stop23", "0730", "T", "")
            )

            val vehicleScheduleFrame: JoreVehicleScheduleFrame = ConversionsFromHastus.convertHastusDataToJore(
                hastusData,
                vehicleTypeIndex,
                dayTypeIndex,
                journeyPatternRefsIndexedByRouteId
            )

            assertEquals(hastusBookingRecordName, vehicleScheduleFrame.label)
            assertEquals(hastusBookingRecordName, vehicleScheduleFrame.name)
            assertEquals("booking", vehicleScheduleFrame.bookingLabel)
            assertEquals("description", vehicleScheduleFrame.bookingDescription)

            assertEquals(2, vehicleScheduleFrame.vehicleServices.size)

            val service1 = vehicleScheduleFrame.vehicleServices[0]

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
                    stopRefList1[0].id,
                    null,
                    4.hours.plus(55.minutes)
                ),
                journey1.passingTimes[0]
            )
            assertEquals(
                JorePassingTime(
                    stopRefList1[1].id,
                    5.hours.plus(10.minutes),
                    5.hours.plus(20.minutes)
                ),
                journey1.passingTimes[1]
            )
            assertEquals(
                JorePassingTime(
                    stopRefList1[2].id,
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
                    stopRefList1[0].id,
                    null,
                    6.hours.plus(55.minutes)
                ),
                journey2.passingTimes[0]
            )
            assertEquals(
                JorePassingTime(
                    stopRefList1[1].id,
                    7.hours.plus(10.minutes),
                    7.hours.plus(20.minutes)
                ),
                journey2.passingTimes[1]
            )
            assertEquals(
                JorePassingTime(
                    stopRefList1[2].id,
                    7.hours.plus(30.minutes),
                    null
                ),
                journey2.passingTimes[2]
            )
        }

        @Test
        fun `when journey pattern reference does not contain a stop point defined in Hastus`() {
            val vehicleTypeIndex = mapOf(
                VEHICLE_TYPE_1_KEY to VEHICLE_TYPE_1_ID
            )
            val dayTypeIndex = mapOf(
                "MA" to MONDAY_DAY_TYPE_ID
            )

            val routeValidityStartDate = LocalDate.of(2023, 1, 1)
            val observationTime: OffsetDateTime = routeValidityStartDate
                .plusDays(1)
                .toOffsetDateTimeAtDefaultZone()

            val stopRefList1 = listOf(
                JoreJourneyPatternStopRef(UUID.randomUUID(), 0, "stop1", "TP1"),
                JoreJourneyPatternStopRef(UUID.randomUUID(), 1, "stop2", null),
                JoreJourneyPatternStopRef(UUID.randomUUID(), 2, "stop3", "TP3")
            )
            val journeyPatternRef1 = JoreJourneyPatternRef(
                journeyPatternRefId = UUID.randomUUID(),
                journeyPatternId = UUID.randomUUID(),
                routeUniqueLabel = "ROUTE-1",
                routeDirection = JoreRouteDirection.OUTBOUND,
                routeValidityStart = routeValidityStartDate,
                routeValidityEnd = LocalDate.of(2050, 12, 31),
                typeOfLine = "stopping_bus_service",
                snapshotTime = DateTimeUtil.currentDateTimeAtDefaultZone(),
                observationTime = observationTime,
                stops = stopRefList1
            )

            val journeyPatternRefsIndexedByRouteId = mapOf(
                journeyPatternRef1.routeLabelAndDirection to journeyPatternRef1
            )

            val hastusData: List<IHastusData> = listOf(
                generateApplicationRecord(),
                generateBookingRecord("name", "booking", "description"),
                generateVehicleScheduleRecord(13),
                generateBlockRecord("block1", "service1", VEHICLE_TYPE_1_KEY),
                generateTripRecord("block1", "trip1", "ROUTE-1", "1", direction = 1),
                generateTripStopRecord("trip1", "stop1", "0455", "T", ""),
                generateTripStopRecord("trip1", "stop4", "0510", "", "t"),
                generateTripStopRecord("trip1", "stop4", "0520", "", ""),
                generateTripStopRecord("trip1", "stop3", "0530", "T", "")
            )

            val exception = assertFailsWith<NoJourneyPatternRefMatchesHastusTripStopsException> {
                ConversionsFromHastus.convertHastusDataToJore(
                    hastusData,
                    vehicleTypeIndex,
                    dayTypeIndex,
                    journeyPatternRefsIndexedByRouteId
                )
            }

            assertEquals(
                "400 BAD_REQUEST \"Hastus trip 'ROUTE-1 (outbound)' contains unknown stop points along the route: 'stop4'\"",
                exception.message
            )
        }
    }

    @DisplayName("Test method: extractRouteLabel")
    @Nested
    inner class TestExtractRouteLabel {

        private fun convertLabelAndVariant(tripRoute: String, variant: String) =
            ConversionsFromHastus.extractRouteLabel(generateTripRecord(tripRoute, variant))

        @Test
        fun `test empty variant`() {
            assertEquals("1", convertLabelAndVariant("1", ""))
        }

        @Test
        fun `test variants that end with '1'`() {
            assertEquals("1", convertLabelAndVariant("1", "1"))
            assertEquals("1A", convertLabelAndVariant("1", "A1"))
            assertEquals("1BC", convertLabelAndVariant("1", "BC1"))
        }

        @Test
        fun `test variants that end with '2'`() {
            assertEquals("1", convertLabelAndVariant("1", "2"))
            assertEquals("1D", convertLabelAndVariant("1", "D2"))
            assertEquals("1EF", convertLabelAndVariant("1", "EF2"))
        }

        @Test
        fun `test single digit variants other than '1' or '2'`() {
            assertEquals("1_3", convertLabelAndVariant("1", "3"))
            assertEquals("1_4", convertLabelAndVariant("1", "4"))
            assertEquals("1_5", convertLabelAndVariant("1", "5"))
            assertEquals("1_6", convertLabelAndVariant("1", "6"))
            assertEquals("1_7", convertLabelAndVariant("1", "7"))
            assertEquals("1_8", convertLabelAndVariant("1", "8"))
            assertEquals("1_9", convertLabelAndVariant("1", "9"))

            // not defined in specs but tested anyway
            assertEquals("1_0", convertLabelAndVariant("1", "0"))
        }

        @Test
        fun `test single char variants`() {
            assertEquals("2A", convertLabelAndVariant("2", "A"))
            assertEquals("2b", convertLabelAndVariant("2", "b"))
        }

        @Test
        fun `test double char variants`() {
            assertEquals("2CD", convertLabelAndVariant("2", "CD"))
            assertEquals("2ef", convertLabelAndVariant("2", "ef"))
        }

        @Test
        fun `test multi-char variants ending with a digit other than '1' or '2'`() {
            assertEquals("3A_3", convertLabelAndVariant("3", "A3"))
            assertEquals("4BC_4", convertLabelAndVariant("4", "BC4"))
        }
    }

    companion object {

        private const val VEHICLE_TYPE_1_KEY = 10
        private val VEHICLE_TYPE_1_ID = UUID.randomUUID()

        private const val VEHICLE_TYPE_2_KEY = 20
        private val VEHICLE_TYPE_2_ID = UUID.randomUUID()

        private val MONDAY_DAY_TYPE_ID = UUID.randomUUID()
        private val FRIDAY_DAY_TYPE_ID = UUID.randomUUID()

        fun generateApplicationRecord() = ApplicationRecord(
            "HASTUS",
            "HSL",
            1.04,
            LocalDate.of(2019, 5, 2),
            LocalTime.of(21, 21, 57)
        )

        fun generateBookingRecord(name: String, booking: String, description: String) = BookingRecord(
            booking,
            description,
            name,
            5,
            LocalDate.of(2019, 8, 12),
            LocalDate.of(2020, 6, 14),
            "CONTRACT"
        )

        fun generateVehicleScheduleRecord(dayType: Int) = VehicleScheduleRecord(
            "4571",
            dayType,
            0,
            "HSL",
            LocalDate.of(2019, 8, 12),
            LocalDate.of(2020, 6, 14),
            LocalDate.of(2019, 4, 30),
            LocalTime.of(11, 38, 55)
        )

        fun generateBlockRecord(block: String, vehicleService: String, vehicleType: Int) = BlockRecord(
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

        fun generateTripRecord(lineLabel: String, variant: String) = generateTripRecord(
            block = "BLOCK-1",
            tripInternalNumber = "TRIP-1",
            lineLabel = lineLabel,
            variant = variant,
            direction = 1
        )

        fun generateTripRecord(
            block: String,
            tripInternalNumber: String,
            lineLabel: String,
            variant: String,
            direction: Int
        ) =
            TripRecord(
                "CONTRACT",
                block,
                tripInternalNumber,
                "1595",
                0,
                lineLabel,
                "4571",
                variant,
                "05:04",
                "05:55",
                60,
                0,
                4,
                25.200,
                "0",
                "p",
                "note2",
                direction,
                2,
                isVehicleTypeMandatory = false,
                isBackupTrip = false,
                isExtraTrip = false
            )

        fun generateTripStopRecord(
            trip: String,
            stop: String,
            time: String,
            type: String,
            note: String
        ) = TripStopRecord(
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
