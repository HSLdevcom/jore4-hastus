package fi.hsl.jore4.hastus.graphql

import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace

data class FetchRoutesResult(
    val lines: List<JoreLine>,
    val stopPoints: List<JoreScheduledStop>,
    val timingPlaces: List<JoreTimingPlace>,
    val distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints>
)
