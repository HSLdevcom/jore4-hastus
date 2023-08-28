package fi.hsl.jore4.hastus.data.jore

data class JoreRouteScheduledStop(
    val stopLabel: String,
    val stopSequenceNumber: Int,
    val timingPlaceCode: String?,
    val isUsedAsTimingPoint: Boolean,
    val isRegulatedTimingPoint: Boolean,
    val isAllowedLoad: Boolean,
    val distanceToNextStop: Double
) {
    /**
     * Timing place name is returned only if the stop point is used as a timing point.
     *
     * This logic is needed while exporting stop points to Hastus.
     */
    val effectiveTimingPlaceCode: String?
        get() = timingPlaceCode?.takeIf { isUsedAsTimingPoint }
}
