package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import java.util.UUID

/**
 * Jore journey pattern
 *
 * @property id The UUID of the journey pattern
 * @property routeUniqueLabel The unique label of the route+variant of the route associated with the journey pattern
 * @property routeDirection The direction of the route associated with the journey pattern
 * @property typeOfLine Type of the line the journey pattern is associated with, for example "regional_bus_service"
 * @property stops The list of stop points in the journey pattern
 */
data class JoreJourneyPattern(
    val id: UUID,
    val routeUniqueLabel: String,
    val routeDirection: JoreRouteDirection,
    val typeOfLine: String,
    val stops: List<JoreStopPoint>
) {
    val routeLabelAndDirection
        get() = RouteLabelAndDirection(routeUniqueLabel, routeDirection)
}
