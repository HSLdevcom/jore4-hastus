package fi.hsl.jore4.hastus.data.hastus

import fi.hsl.jore4.hastus.Constants
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Application record
 *
 * @property hastus Constant HASTUS
 * @property companyName Constant HSL. Ignored
 * @property vscVersion Control file version
 * @property jobDate Creation date
 * @property jobTime Creation time
 * @constructor Create an Application record from a list of strings
 */
data class ApplicationRecord(
    val hastus: String,
    val companyName: String,
    val vscVersion: Number,
    val jobDate: LocalDate,
    val jobTime: LocalTime
) : HastusData() {

    constructor(elements: List<String>) : this(
        hastus = elements[1],
        companyName = elements[2],
        vscVersion = parseToDouble(elements[3]),
        jobDate = LocalDate.parse(elements[4], dateFormatter),
        jobTime = LocalTime.parse(elements[5].substring(0, 6), timeFormatter)
    )

    override fun getFields(): List<Any> {
        return listOf()
    }

    companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DEFAULT_HASTUS_DATE_FORMAT)
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(Constants.DEFAULT_HASTUS_TIME_FORMAT)
    }
}
