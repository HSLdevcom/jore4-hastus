package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CannotFindJourneyPatternRefByTimingPlaceLabelsException(
    message: String
) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    message
) {

    constructor(
        routeIdentifier: RouteLabelAndDirection,
        stopLabels: List<String>,
        placeCodes: List<String?>
    ) : this(
        """
        Could not find matching journey pattern reference whose timing place labels correspond to the Hastus trip.

        Trip label: ${routeIdentifier.routeLabel},
        Trip direction: ${routeIdentifier.direction.wellKnownNumber},
        Stop points: $stopLabels,
        Place codes: ${placeCodes.map { /* replace nulls with empty strings */ it ?: "" }}
        """.trimIndent()
    )
}
