package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.service.exporting.ConversionsToHastus
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CannotFindJourneyPatternRefByStopPointLabelsException(
    message: String
) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    message
) {

    constructor(
        routeIdentifier: RouteLabelAndDirection,
        stopLabels: List<String>
    ) : this(
        """
        Could not find matching journey pattern reference whose stop points correspond to the Hastus trip.

        Trip label: ${routeIdentifier.routeLabel},
        Trip direction: ${
            // This is safe to call here. Possible exceptions in conversions have already taken place.
            ConversionsToHastus.convertRouteDirection(routeIdentifier.direction)
        },
        Stop points: $stopLabels
        """.trimIndent()
    )
}
