package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CannotFindJourneyPatternRefByStopPointLabelsException(
    val routeIdentifier: RouteLabelAndDirection,
    val stopLabels: List<String>
) : ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        constructErrorMessage(routeIdentifier, stopLabels)
    ) {
    companion object {
        private fun constructErrorMessage(
            routeIdentifier: RouteLabelAndDirection,
            stopLabels: List<String>
        ): String =
            """
            Could not find matching journey pattern reference whose stop points correspond to the Hastus trip.
            
            Trip label: ${routeIdentifier.routeLabel},
            Trip direction: ${routeIdentifier.direction.wellKnownNumber},
            Stop points: $stopLabels
            """.trimIndent()
    }
}
