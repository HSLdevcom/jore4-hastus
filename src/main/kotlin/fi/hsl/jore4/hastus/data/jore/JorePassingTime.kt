package fi.hsl.jore4.hastus.data.jore

import java.util.UUID
import kotlin.time.Duration

/**
 * Jore passing time
 *
 * @property associatedStop UUID of the passed stop
 * @property arrivalTime Possible arrival time represented as a duration
 * @property departureTime Possible departure time represented as a duration
 */
data class JorePassingTime(
    val associatedStop: UUID,
    val arrivalTime: Duration?,
    val departureTime: Duration?
)
