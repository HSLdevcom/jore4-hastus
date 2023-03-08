package fi.hsl.jore4.hastus.data.jore

import java.util.*

data class JoreJourneyPatternReference(
    val id: UUID,
    val journeyPatternId: UUID,
    val stops: List<JoreStopReference>
)
