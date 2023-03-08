package fi.hsl.jore4.hastus.data.jore

import java.util.*

/**
 * Jore vehicle service
 *
 * @property name Name of this vehicle service
 * @property dayType Day type for this vehicle service
 * @property blocks Blocks in the vehicle service
 */
data class JoreVehicleService(
    val name: String,
    val dayType: UUID,
    val blocks: List<JoreBlock>
)
