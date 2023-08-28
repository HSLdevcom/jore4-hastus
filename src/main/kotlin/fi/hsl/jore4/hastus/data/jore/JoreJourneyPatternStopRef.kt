package fi.hsl.jore4.hastus.data.jore

import java.util.UUID

data class JoreJourneyPatternStopRef(
    val id: UUID,
    val stopSequenceNumber: Int,
    val stopLabel: String,
    val timingPlaceCode: String?
)
