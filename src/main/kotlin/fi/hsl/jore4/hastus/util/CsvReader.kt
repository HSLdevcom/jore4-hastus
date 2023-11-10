package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.data.hastus.imp.ApplicationRecord
import fi.hsl.jore4.hastus.data.hastus.imp.BlockRecord
import fi.hsl.jore4.hastus.data.hastus.imp.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.imp.ImportableItem
import fi.hsl.jore4.hastus.data.hastus.imp.TripRecord
import fi.hsl.jore4.hastus.data.hastus.imp.TripStopRecord
import fi.hsl.jore4.hastus.data.hastus.imp.VehicleScheduleRecord
import mu.KotlinLogging

class CsvReader(
    private val separator: String = ";"
) {

    fun parseCsv(file: String): List<ImportableItem> {
        return file.split("\n").mapNotNull { parseLine(it) }
    }

    private fun parseLine(line: String): ImportableItem? {
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

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }
}
