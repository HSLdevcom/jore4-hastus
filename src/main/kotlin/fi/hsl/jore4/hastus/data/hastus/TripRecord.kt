package fi.hsl.jore4.hastus.data.hastus

/**
 * Trip record
 *
 * @property contract Contract label
 * @property blockNumber Internal number for the block this trip is in
 * @property tripInternalNumber Internal number for this trip
 * @property tripNumber User defined number for the trip
 * @property tripType Type of the trip. 0 = normal, 1 = pull out, 2 = pull in, 3 = transfer
 * @property tripRoute Route (Jore4 line) for the trip
 * @property tripDisplayedName Displayed name for the trip
 * @property variant Variant, see specific documentation how this is formed
 * @property startTime Starting time, can be > 24.00
 * @property endTime Ending time, can be > 24.00
 * @property duration Duration for the trip in minutes
 * @property turnaroundTime Turnaround time, the time reserved to proceed from the end of a trip to the start of another
 * @property layoverTime Layover time; a buffer time allowance at the end of a trip
 * @property distance Total distance of the trip
 * @property note Special note. Value 'p' means it's a friday trip
 * @property note2 Reserved for extra information
 * @property direction Direction. 1 = outbound, 2 = inbound
 * @property vehicleType HSL vehicle type id
 * @property isVehicleTypeMandatory Is vehicle type mandatory
 * @property isBackupTrip Is this a backup trip
 * @property isExtraTrip Is this an extra trip
 * @constructor Create a Trip record from a list of strings
 */
data class TripRecord(
    val contract: String,
    val blockNumber: String,
    val tripInternalNumber: String,
    val tripNumber: String,
    val tripType: Int,
    val tripRoute: String,
    val tripDisplayedName: String,
    val variant: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val turnaroundTime: Int,
    val layoverTime: Int,
    val distance: Double,
    val note: String,
    val note2: String,
    val direction: Int,
    val vehicleType: Int,
    val isVehicleTypeMandatory: Boolean,
    val isBackupTrip: Boolean,
    val isExtraTrip: Boolean
) : HastusData() {

    constructor(elements: List<String>) : this(
        contract = elements[1],
        blockNumber = elements[2],
        tripInternalNumber = elements[3],
        tripNumber = elements[4],
        tripType = parseToInt(elements[5]),
        tripRoute = elements[6],
        tripDisplayedName = elements[7],
        variant = elements[8],
        startTime = elements[9],
        endTime = elements[10],
        duration = parseToInt(elements[11]),
        turnaroundTime = parseToInt(elements[12]),
        layoverTime = parseToInt(elements[13]),
        distance = parseToDouble(elements[14]),
        note = elements[15],
        note2 = elements[16],
        direction = parseToInt(elements[17]),
        vehicleType = parseToInt(elements[18]),
        isVehicleTypeMandatory = parseToBoolean(elements[19]),
        isBackupTrip = parseToBoolean(elements[20]),
        isExtraTrip = parseToBoolean(elements[21])
    )

    override fun getFields(): List<Any> {
        return listOf()
    }

    override fun toString(): String {
        return "TripRecord(contract='$contract', blockNumber='$blockNumber', tripInternalNumber='$tripInternalNumber', tripNumber='$tripNumber', tripType=$tripType, tripRoute='$tripRoute', tripDisplayedName='$tripDisplayedName', variant='$variant', startTime='$startTime', endTime='$endTime', duration=$duration, turnaroundTime=$turnaroundTime, layoverTime=$layoverTime, distance=$distance, note='$note', note2='$note2', direction=$direction, vehicleType=$vehicleType, isVehicleTypeMandatory=$isVehicleTypeMandatory, isBackupTrip=$isBackupTrip, isExtraTrip=$isExtraTrip)"
    }
}
