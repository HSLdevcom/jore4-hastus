package fi.hsl.jore4.hastus.data.hastus.imp

import java.time.LocalDate

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
) : ImportableItem() {

    constructor(elements: List<String>) : this(
        booking = elements[1],
        bookingDescription = elements[2],
        name = elements[3],
        scheduleDayType = parseToInt(elements[4]),
        startDate = LocalDate.parse(elements[5], dateFormatter()),
        endDate = LocalDate.parse(elements[6], dateFormatter()),
        contract = elements[7]
    )
}
