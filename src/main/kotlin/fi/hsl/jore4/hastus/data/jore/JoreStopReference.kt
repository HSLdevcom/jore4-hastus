package fi.hsl.jore4.hastus.data.jore

import java.util.*

data class JoreStopReference(
    val stopId: UUID,
    val stopLabel: String,
    val sequence: Int
)
