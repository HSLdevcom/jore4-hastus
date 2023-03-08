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
    override val fieldName = "3"

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
        return listWithFieldName()
    }

    override fun toString(): String {
        return "VehicleScheduleRecord(name='$name', scheduleType='$scheduleType', scenario=$scenario, startDate=$startDate, endDate=$endDate, editDate=$editDate, editTime=$editTime, fieldName='$fieldName')"
    }

    companion object {
        private const val dateFormat = "yyyyMMdd"
        private const val timeFormat = "HHmmss"

        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(timeFormat)
    }
}
