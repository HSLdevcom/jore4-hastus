package fi.hsl.jore4.hastus.data.jore

data class JoreRouteScheduledStop(
    val stopLabel: String,
    val timingPlaceShortName: String?,
    val isUsedAsTimingPoint: Boolean,
    val isRegulatedTimingPoint: Boolean,
    val isAllowedLoad: Boolean,
    val distanceToNextStop: Double
)
