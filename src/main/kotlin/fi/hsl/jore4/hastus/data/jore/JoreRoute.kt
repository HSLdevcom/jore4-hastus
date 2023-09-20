package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection

data class JoreRoute(
    val label: String,
    val variant: String,
    val uniqueLabel: String,
    val name: String,
    val direction: JoreRouteDirection,
    val reversible: Boolean,
    val stopsOnRoute: List<JoreRouteScheduledStop>
)
