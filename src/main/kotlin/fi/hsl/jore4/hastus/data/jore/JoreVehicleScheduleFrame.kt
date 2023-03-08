package fi.hsl.jore4.hastus.data.jore

import java.time.LocalDate

/**
 * Jore vehicle schedule frame
 *
 * @property name Name of this vehicle schedule frame
 * @property label Label given for this vehicle schedule frame
 * @property bookingLabel Label of the associated booking
 * @property bookingDescription Description of the associated booking
 * @property validityStart Date from when this frame is in effect
 * @property validityEnd Date when this frame is no longer in effect
 * @property vehicleServices List of vehicle services in this schedule frame
 */
data class JoreVehicleScheduleFrame(
    val name: String,
    val label: String,
    val bookingLabel: String,
    val bookingDescription: String,
    val validityStart: LocalDate,
    val validityEnd: LocalDate,
    val vehicleServices: List<JoreVehicleService>
)
