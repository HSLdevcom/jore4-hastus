package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.Coordinate

data class JoreScheduledStop(
    val label: String,
    val platform: String,
    val nameFinnish: String,
    val nameSwedish: String,
    val streetNameFinnish: String,
    val streetNameSwedish: String,
    val timingPlaceShortName: String,
    val location: Coordinate
)
