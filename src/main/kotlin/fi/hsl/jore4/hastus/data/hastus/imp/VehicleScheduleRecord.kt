package fi.hsl.jore4.hastus.data.hastus.imp

import java.time.LocalDate
import java.time.LocalTime

/**
 * Vehicle schedule record
 *
 * @property name Displayed name for the vehicle schedule
 * @property scheduleType Day type
 * @property scenario Hastus scenario number
 * @property owner Owner of this schedule
 * @property startDate Start date for the vehicle schedule
 * @property endDate End date for the vehicle schedule
 * @property editDate Date when this was last edited
 * @property editTime Time when this was last edited
 * @constructor Create a vehicle schedule record from a list of strings
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
) : ImportableItem() {
    constructor(elements: List<String>) : this(
        name = elements[1],
        scheduleType = parseToInt(elements[2]),
        scenario = parseToInt(elements[3]),
        owner = elements[4],
        startDate = LocalDate.parse(elements[5], dateFormatter()),
        endDate = LocalDate.parse(elements[6], dateFormatter()),
        editDate = LocalDate.parse(elements[7], dateFormatter()),
        editTime = LocalTime.parse(elements[8], timeFormatter())
    )
}
