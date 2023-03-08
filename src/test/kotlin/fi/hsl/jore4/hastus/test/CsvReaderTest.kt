package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import fi.hsl.jore4.hastus.util.CsvReader
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals

@DisplayName("Test the CSV reader")
class CsvReaderTest {

    @Test
    @DisplayName("When reading CSV")
    fun readCsvTest() {
        val csv = """
            1;HASTUS;HSL;1.04;20190502;212157
            2;19SYK;Syksy 2019 - Kevät 2020;4571;05;20190812;20200614;CONTRACT
            3;4571;05;00;HSL;20190812;20200614;20190430;113855
            4;1462351;4571- 1;1;4VARIS;4VARIS;4571; 3; ;0
            5;CONTRACT;1462351;12766047;1595;0;4571;4571;2;05:04;05:55; 60; 0; 4;25.200;0;;1;2;0;0;0;0
            6;12766047;4VARIS;4140233;;;;;0504;0.0;T;
            6;12766047;;4140242;;;;;0505;298.0;;
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
            "4VARIS",
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

        val expectedTripStopRecord1 = TripStopRecord(
            "12766047",
            "4VARIS",
            "4140233",
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
            "",
            "4140242",
            "",
            "",
            "",
            "",
            "0505",
            298.0,
            "",
            ""
        )

        val csvReader = CsvReader(";")

        val parsedResult = csvReader.parseCsv(csv)

        assertEquals(7, parsedResult.size)

        val applicationRecord: ApplicationRecord = parsedResult[0] as ApplicationRecord
        val bookingRecord: BookingRecord = parsedResult[1] as BookingRecord
        val vehicleScheduleRecord: VehicleScheduleRecord = parsedResult[2] as VehicleScheduleRecord
        val blockRecord: BlockRecord = parsedResult[3] as BlockRecord
        val tripRecord: TripRecord = parsedResult[4] as TripRecord
        val tripStopRecord1: TripStopRecord = parsedResult[5] as TripStopRecord
        val tripStopRecord2: TripStopRecord = parsedResult[6] as TripStopRecord

        assertEquals(expectedApplicationRecord, applicationRecord)
        assertEquals(expectedBookingRecord, bookingRecord)
        assertEquals(expectedVehicleScheduleRecord, vehicleScheduleRecord)
        assertEquals(expectedBlockRecord, blockRecord)
        assertEquals(expectedTripRecord, tripRecord)
        assertEquals(expectedTripStopRecord1, tripStopRecord1)
        assertEquals(expectedTripStopRecord2, tripStopRecord2)
    }
}
