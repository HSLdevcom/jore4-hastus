package fi.hsl.jore4.hastus.data.jore

class JoreRoute(
    val label: String,
    val variant: String,
    val uniqueLabel: String,
    val name: String,
    val direction: Int,
    val reversible: Boolean,
    val stopsOnRoute: List<JoreRouteScheduledStop>
)
