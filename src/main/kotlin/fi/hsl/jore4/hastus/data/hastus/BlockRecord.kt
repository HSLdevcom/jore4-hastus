package fi.hsl.jore4.hastus.data.hastus

/**
 * Block record
 *
 * @property internalNumber Internal identifying number to connect to other elements
 * @property vehicleServiceName Name of the vehicle service this block belongs in
 * @property sequence Sequence of the block in the vehicle service
 * @property startTimingPlace Starting timing place for the block
 * @property endTimingPlace Ending timing place for the block
 * @property mainRoute Route (Jore4 line) which is considered the main for the block
 * @property prepOutTime Preparing time before the block
 * @property prepInTime Preparing time after the block
 * @property vehicleType Vehicle type HSL id
 * @constructor Create Block record from a list of strings
 */
data class BlockRecord(
    val internalNumber: String,
    val vehicleServiceName: String,
    val sequence: Int,
    val startTimingPlace: String,
    val endTimingPlace: String,
    val mainRoute: String,
    val prepOutTime: Int,
    val prepInTime: Int,
    val vehicleType: Int
) : HastusData() {

    constructor(elements: List<String>) : this(
        internalNumber = elements[1],
        vehicleServiceName = elements[2],
        sequence = parseToInt(elements[3]),
        startTimingPlace = elements[4],
        endTimingPlace = elements[5],
        mainRoute = elements[6],
        prepOutTime = parseToInt(elements[7]),
        prepInTime = parseToInt(elements[8]),
        vehicleType = parseToInt(elements[9])
    )

    override fun getFields(): List<Any> {
        return listOf()
    }
}
