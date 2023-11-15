package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CannotFindJourneyPatternRefByTimingPlaceLabelsException(
    val routeIdentifier: RouteLabelAndDirection,
    val stopLabels: List<String>,
    val placeCodes: List<String?>
) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    constructErrorMessage(routeIdentifier, stopLabels, placeCodes)
) {
    companion object {
        private fun constructErrorMessage(
            routeIdentifier: RouteLabelAndDirection,
            stopLabels: List<String>,
            placeCodes: List<String?>
        ): String {
            return """
            Could not find matching journey pattern reference whose timing place labels correspond to the Hastus trip.
    
            Trip label: ${routeIdentifier.routeLabel},
            Trip direction: ${routeIdentifier.direction.wellKnownNumber},
            Stop points with place codes: ${
                stopLabels
                    .zip(placeCodes)
                    .map { (stopLabel, nullablePlaceCode) ->
                        nullablePlaceCode
                            ?.let { "$stopLabel:$it" }
                            ?: stopLabel
                    }
            }
            """.trimIndent()
        }
    }
}
