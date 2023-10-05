package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals

@DisplayName("Test the CSV reader")
class CsvReaderTest {

    @Test
    fun `when reading CSV`() {
        val csv = """
            1;HASTUS;HSL;1.04;20190502;212157
            2;19SYK;Syksy 2019 - Kevät 2020;4571;05;20190812;20200614;CONTRACT
            3;4571;05;00;HSL;20190812;20200614;20190430;113855
            4;1462351;4571- 1;1;4VARIS;1AURLA;4571;3;;0
            5;CONTRACT;1462351;12766047;1595;0;4571;4571;2;05:04;05:08;10;0;4;0.513;0;p;note2;1;2;0;0;0;0
            6;12766047;4VARIS;H1234;;;;;0504;0.0;T;
            6;12766047;;H1235;;;;;0505;;;
            6;12766047;1AACKT;H1236;;;;;0506;334.0;R;t
            6;12766047;1AACKT;H1236;;;;;0507;334.0;R;a
            6;12766047;1AURLA;H1237;;;;;0508;179.0;T;
        """.trimIndent()

        val expectedApplicationRecord = ApplicationRecord(
            "HASTUS",
            "HSL",
            1.04,
            LocalDate.of(2019, 5, 2),
            LocalTime.of(21, 21, 57)
        )

        val expectedBookingRecord = BookingRecord(
            "19SYK",
            "Syksy 2019 - Kevät 2020",
            "4571",
            5,
            LocalDate.of(2019, 8, 12),
            LocalDate.of(2020, 6, 14),
            "CONTRACT"
        )

        val expectedVehicleScheduleRecord = VehicleScheduleRecord(
            "4571",
            5,
            0,
            "HSL",
            LocalDate.of(2019, 8, 12),
            LocalDate.of(2020, 6, 14),
            LocalDate.of(2019, 4, 30),
            LocalTime.of(11, 38, 55)
        )

        val expectedBlockRecord = BlockRecord(
            "1462351",
            "4571- 1",
            1,
            "4VARIS",
            "1AURLA",
            "4571",
            3,
            0,
            0
        )

        val expectedTripRecord = TripRecord(
            "CONTRACT",
            "1462351",
            "12766047",
            "1595",
            0,
            "4571",
            "4571",
            "2",
            "05:04",
            "05:08",
            10,
            0,
            4,
            0.513,
            "0",
            "p",
            "note2",
            1,
            2,
            isVehicleTypeMandatory = false,
            isBackupTrip = false,
            isExtraTrip = false
        )

        val expectedTripStopRecord1 = TripStopRecord(
            "12766047",
            "4VARIS",
            "H1234",
            "",
            "",
            "",
            "",
            "0504",
            0.0,
            "T",
            ""
        )

        val expectedTripStopRecord2 = TripStopRecord(
            "12766047",
            null,
            "H1235",
            "",
            "",
            "",
            "",
            "0505",
            null,
            "",
            ""
        )

        val expectedTripStopRecord3 = TripStopRecord(
            "12766047",
            "1AACKT",
            "H1236",
            "",
            "",
            "",
            "",
            "0506",
            334.0,
            "R",
            "t"
        )
        val expectedTripStopRecord4 = TripStopRecord(
            "12766047",
            "1AACKT",
            "H1236",
            "",
            "",
            "",
            "",
            "0507",
            334.0,
            "R",
            "a"
        )
        val expectedTripStopRecord5 = TripStopRecord(
            "12766047",
            "1AURLA",
            "H1237",
            "",
            "",
            "",
            "",
            "0508",
            179.0,
            "T",
            ""
        )

        val csvReader = CsvReader(";")

        val parsedResult = csvReader.parseCsv(csv)

        assertEquals(10, parsedResult.size)

        val applicationRecord: ApplicationRecord = parsedResult[0] as ApplicationRecord
        val bookingRecord: BookingRecord = parsedResult[1] as BookingRecord
        val vehicleScheduleRecord: VehicleScheduleRecord = parsedResult[2] as VehicleScheduleRecord
        val blockRecord: BlockRecord = parsedResult[3] as BlockRecord
        val tripRecord: TripRecord = parsedResult[4] as TripRecord
        val tripStopRecord1: TripStopRecord = parsedResult[5] as TripStopRecord
        val tripStopRecord2: TripStopRecord = parsedResult[6] as TripStopRecord
        val tripStopRecord3: TripStopRecord = parsedResult[7] as TripStopRecord
        val tripStopRecord4: TripStopRecord = parsedResult[8] as TripStopRecord
        val tripStopRecord5: TripStopRecord = parsedResult[9] as TripStopRecord

        assertEquals(expectedApplicationRecord, applicationRecord)
        assertEquals(expectedBookingRecord, bookingRecord)
        assertEquals(expectedVehicleScheduleRecord, vehicleScheduleRecord)
        assertEquals(expectedBlockRecord, blockRecord)
        assertEquals(expectedTripRecord, tripRecord)
        assertEquals(expectedTripStopRecord1, tripStopRecord1)
        assertEquals(expectedTripStopRecord2, tripStopRecord2)
        assertEquals(expectedTripStopRecord3, tripStopRecord3)
        assertEquals(expectedTripStopRecord4, tripStopRecord4)
        assertEquals(expectedTripStopRecord5, tripStopRecord5)
    }
}
