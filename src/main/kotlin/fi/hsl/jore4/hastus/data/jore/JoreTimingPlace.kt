package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.Constants
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.timing_pattern_timing_place

data class JoreTimingPlace(
    val label: String,
    val description: String
) {
    companion object {

        fun from(timingPlace: timing_pattern_timing_place): JoreTimingPlace {
            val description: String = timingPlace
                .description
                ?.content
                ?.get(Constants.LANG_FINNISH)
                ?: timingPlace.label // Use label as description if one is not provided

            return JoreTimingPlace(timingPlace.label, description)
        }
    }
}
