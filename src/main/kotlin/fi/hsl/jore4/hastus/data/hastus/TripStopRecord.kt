package fi.hsl.jore4.hastus.data.hastus

/**
 * Trip stop record
 *
 * @property tripInternalNumber The trip number this stop is a part of
 * @property timingPlace Timing place for the stop
 * @property stopId 7 number code for the stop
 * @property stopZone Not used
 * @property stopDistrict Not used
 * @property xCoordinate Not used
 * @property yCoordinate Not used
 * @property passingTime Passing time for the stop
 * @property distanceFromPreviousStop Distance from the previous stop in meters
 * @property stopType Type of the stop. 'R' on timing places, 'T' on first and last stop, otherwise empty
 * @property note Used to note a reguled timing point. 'a' to mark leaving time, 't' to mark arrival time
 * @constructor Create a Trip stop record from a list of strings
 */
data class TripStopRecord(
    val tripInternalNumber: String,
    val timingPlace: String,
    val stopId: String,
    val stopZone: String,
    val stopDistrict: String,
    val xCoordinate: String,
    val yCoordinate: String,
    val passingTime: String,
    val distanceFromPreviousStop: Double,
    val stopType: String,
    val note: String
) : HastusData() {

    constructor(elements: List<String>) : this(
        tripInternalNumber = elements[1],
        timingPlace = elements[2],
        stopId = elements[3],
        stopZone = elements[4],
        stopDistrict = elements[5],
        xCoordinate = elements[6],
        yCoordinate = elements[7],
        passingTime = elements[8],
        distanceFromPreviousStop = parseToDouble(elements[9]),
        stopType = elements[10],
        note = elements[11]
    )

    override fun getFields(): List<Any> {
        return listOf()
    }

    override fun toString(): String {
        return "TripStopRecord(tripInternalNumber='$tripInternalNumber', timingPlace='$timingPlace', stopId='$stopId', stopZone='$stopZone', stopDistrict='$stopDistrict', xCoordinate='$xCoordinate', yCoordinate='$yCoordinate', passingTime='$passingTime', distanceFromPreviousStop=$distanceFromPreviousStop, stopType='$stopType', note='$note')"
    }
}
