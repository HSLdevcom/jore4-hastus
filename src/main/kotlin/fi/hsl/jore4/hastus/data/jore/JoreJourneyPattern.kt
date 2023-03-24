package fi.hsl.jore4.hastus.data.jore

import java.util.UUID

/**
 * Jore journey pattern
 *
 * @property uniqueLabel Unique label of the route+variant of the journey pattern
 * @property journeyPatternId UUID of the journey pattern
 * @property typeOfLine Type of the line the journey patter is on, for example "regional_bus_service"
 * @property stops List of stop points in the journey pattern
 */
data class JoreJourneyPattern(
    val uniqueLabel: String?,
    val journeyPatternId: UUID?,
    val typeOfLine: String,
    val stops: List<JoreStopPoint>
)
