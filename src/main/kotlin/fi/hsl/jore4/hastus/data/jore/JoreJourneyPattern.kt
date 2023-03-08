package fi.hsl.jore4.hastus.data.jore

import java.util.*

/**
 * Jore journey pattern
 *
 * @property uniqueLabel Unique label of the route+variant of the journey pattern
 * @property journeyPatternId UUID of the journey pattern
 * @property stops List of stop points in the journey pattern
 */
data class JoreJourneyPattern(
    val uniqueLabel: String?,
    val journeyPatternId: UUID?,
    val stops: List<JoreStopPoint>
)
