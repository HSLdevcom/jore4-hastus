package fi.hsl.jore4.hastus.data.hastus

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Vehicle schedule record
 *
 * @property name Displayed name for the vehicle schedule
 * @property scheduleType Day type
 * @property scenario Hastus scenario number
 * @property owner Owner of this shcedule
 * @property startDate Start date for the vehicle schedule
 * @property endDate End date for the vehicle schedule
 * @property editDate Date when this was last edited
 * @property editTime Time when this was last edited
 * @constructor Create a vehicle scheduel record from a list of strings
 */
data class VehicleScheduleRecord(
    val name: String,
    val scheduleType: Int,
    val scenario: Int,
    val owner: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val editDate: LocalDate,
    val editTime: LocalTime
) : HastusData() {

    constructor(elements: List<String>) : this(
        name = elements[1],
        scheduleType = parseToInt(elements[2]),
        scenario = parseToInt(elements[3]),
        owner = elements[4],
        startDate = LocalDate.parse(elements[5], dateFormatter),
        endDate = LocalDate.parse(elements[6], dateFormatter),
        editDate = LocalDate.parse(elements[7], dateFormatter),
        editTime = LocalTime.parse(elements[8], timeFormatter)
    )

    override fun getFields(): List<Any> {
        return listOf()
    }

    companion object {
        private const val DATE_FORMAT = "yyyyMMdd"
        private const val TIME_FORMAT = "HHmmss"

        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT)
    }
}
