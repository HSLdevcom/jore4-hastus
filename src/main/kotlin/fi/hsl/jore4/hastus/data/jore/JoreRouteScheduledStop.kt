package fi.hsl.jore4.hastus.data.jore

data class JoreRouteScheduledStop(
    val timingPlaceShortName: String?,
    val distanceToNextStop: Double,
    val isRegulatedTimingPoint: Boolean,
    val isAllowedLoad: Boolean,
    val isTimingPoint: Boolean,
    val stopLabel: String
)
