package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import java.time.LocalDate
import java.util.UUID

/**
 * @property[typeOfLine] Type of line is a redundant piece of information since it appears also in
 * [JoreLine] class. For optimisation purposes, it is copied here.
 */
data class JoreRoute(
    val label: String,
    val variant: String?,
    val name: String,
    val direction: JoreRouteDirection,
    val reversible: Boolean,
    val validityStart: LocalDate?,
    val validityEnd: LocalDate?,
    val typeOfLine: String,
    val journeyPatternId: UUID,
    val stopsOnRoute: List<JoreRouteScheduledStop>
) {
    val uniqueLabel: String
        get() = "$label${
            variant?.let { "_$it" } ?: ""
        }"
}
