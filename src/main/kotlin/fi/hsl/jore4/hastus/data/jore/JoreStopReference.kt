package fi.hsl.jore4.hastus.data.jore

import java.util.UUID

data class JoreStopReference(
    val stopId: UUID,
    val stopLabel: String,
    val sequence: Int
)
