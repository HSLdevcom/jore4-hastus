package fi.hsl.jore4.hastus.data.jore

import java.util.UUID
import kotlin.time.Duration

/**
 * Jore block
 *
 * @property journeyName Displayed name for the block
 * @property preparingTime Preparing time
 * @property finishingTime Finishing time
 * @property vehicleTypeId The identifier of the vehicle type (as UUID key in database)
 * @property vehicleJourneys List of vehicle journeys in the block
 */
data class JoreBlock(
    val journeyName: String,
    val preparingTime: Duration,
    val finishingTime: Duration,
    val vehicleTypeId: UUID,
    val vehicleJourneys: List<JoreVehicleJourney>
)
