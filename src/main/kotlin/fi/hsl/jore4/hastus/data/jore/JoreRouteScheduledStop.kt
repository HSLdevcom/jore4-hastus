package fi.hsl.jore4.hastus.data.jore

data class JoreRouteScheduledStop(
    val hastusPlace: String,
    val distance: Double,
    val isInUse: Boolean,
    val isAllowedLoad: Boolean,
    val isTimingPoint: Boolean,
    val stopLabel: String
)
