package fi.hsl.jore4.hastus.data.hastus.imp

/**
 * Hastus trip stop record
 *
 * @property tripInternalNumber The trip number this stop point is part of
 * @property timingPlace Timing place code for the stop point
 * @property stopId The label of the stop point
 * @property stopZone Not used
 * @property stopDistrict Not used
 * @property xCoordinate Not used
 * @property yCoordinate Not used
 * @property passingTime Passing time for the stop point
 * @property distanceFromPreviousStop Distance from the previous timing point in meters. Null, if
 * this stop point is not a timing point.
 * @property stopType Type of the stop point. 'R' on timing places, 'T' on the first and last stop
 * point, otherwise empty
 * @property note Used to mark a regulated timing point. 'a' to mark leaving time, 't' to mark
 * arrival time
 *
 * @constructor Create a Trip stop record from a list of strings
 */
data class TripStopRecord(
    val tripInternalNumber: String,
    val timingPlace: String?,
    val stopId: String,
    val stopZone: String,
    val stopDistrict: String,
    val xCoordinate: String,
    val yCoordinate: String,
    val passingTime: String,
    val distanceFromPreviousStop: Double?,
    val stopType: String,
    val note: String
) : ImportableItem() {
    val isUsedAsTimingPoint: Boolean
        get() = stopType == "T" || stopType == "R"

    // The value of timing place is taken if the stop point is used as a timing point in journey
    // pattern.
    val effectiveTimingPlace: String?
        get() = timingPlace?.takeIf { isUsedAsTimingPoint }

    constructor(elements: List<String>) : this(
        tripInternalNumber = elements[1],
        timingPlace = elements[2].takeIf { it.isNotBlank() },
        stopId = elements[3],
        stopZone = elements[4],
        stopDistrict = elements[5],
        xCoordinate = elements[6],
        yCoordinate = elements[7],
        passingTime = elements[8],
        distanceFromPreviousStop = elements[9].toDoubleOrNull(),
        stopType = elements[10],
        note = elements[11]
    )
}
