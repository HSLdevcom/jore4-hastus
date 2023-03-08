package fi.hsl.jore4.hastus.data.hastus

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Booking record
 *
 * @property booking The name of the booking which contains this schedule
 * @property bookingDescription Description of the booking
 * @property name Name for the schedule
 * @property scheduleDayType Day type of the schedule
 * @property startDate Validity start date
 * @property endDate Validity end date
 * @property contract Contract label
 * @constructor Create a Booking record from a list of strings
 */
data class BookingRecord(
    val booking: String,
    val bookingDescription: String,
    val name: String,
    val scheduleDayType: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val contract: String
) : HastusData() {

    constructor(elements: List<String>) : this(
        booking = elements[1],
        bookingDescription = elements[2],
        name = elements[3],
        scheduleDayType = parseToInt(elements[4]),
        startDate = LocalDate.parse(elements[5], BookingRecord.dateFormatter),
        endDate = LocalDate.parse(elements[6], BookingRecord.dateFormatter),
        contract = elements[7]
    )

    override val fieldName = "2"

    override fun getFields(): List<Any> {
        return listWithFieldName()
    }

    override fun toString(): String {
        return "BookingRecord(booking='$booking', bookingDescription='$bookingDescription', name='$name', scheduleDayType=$scheduleDayType, startDate=$startDate, endDate=$endDate, contract='$contract', fieldName='$fieldName')"
    }

    companion object {
        private const val dateFormat = "yyyyMMdd"

        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
    }
}
