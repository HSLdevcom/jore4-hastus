package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.Coordinate
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point

data class JoreScheduledStop(
    val label: String,
    val platform: String,
    val nameFinnish: String,
    val nameSwedish: String,
    val streetNameFinnish: String,
    val streetNameSwedish: String,
    val timingPlaceShortName: String?,
    val location: Coordinate
) {
    companion object {

        fun from(stop: service_pattern_scheduled_stop_point) = JoreScheduledStop(
            stop.label,
            "00", // TODO
            "kuvaus", // TODO
            "beskrivning", // TODO
            "katu", // TODO
            "gata", // TODO
            stop.timing_place?.label,
            stop.measured_location
        )
    }
}
