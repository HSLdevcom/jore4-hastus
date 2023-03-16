package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.data.hastus.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.hastus.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.VehicleScheduleRecord
import mu.KotlinLogging

class CsvReader(
    private val separator: String = ";"
) {

    fun parseCsv(file: String): List<IHastusData> {
        return file.split("\n").mapNotNull { parseLine(it) }
    }

    private fun parseLine(line: String): IHastusData? {
        val values = line.trim().split(separator)
        return when (values[0]) {
            "1" -> ApplicationRecord(values)
            "2" -> BookingRecord(values)
            "3" -> VehicleScheduleRecord(values)
            "4" -> BlockRecord(values)
            "5" -> TripRecord(values)
            "6" -> TripStopRecord(values)
            else -> {
                LOGGER.info("CSV parser ignored line $line")
                null
            }
        }
    }
}
