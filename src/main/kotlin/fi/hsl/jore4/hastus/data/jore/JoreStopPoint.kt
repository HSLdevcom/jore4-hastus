package fi.hsl.jore4.hastus.data.jore

import java.util.UUID

data class JoreStopPoint(
    val id: UUID,
    val label: String,
    val sequenceNumber: Int
)
