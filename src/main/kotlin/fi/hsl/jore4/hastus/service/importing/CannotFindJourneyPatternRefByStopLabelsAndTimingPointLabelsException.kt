package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.service.exporting.ConversionsToHastus
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CannotFindJourneyPatternRefByStopLabelsAndTimingPointLabelsException(
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
        No journey pattern reference was found whose stop points correspond to the Hastus trip.

        Trip label: ${routeIdentifier.routeLabel},
        Trip direction: ${
            // This is safe to call here. Possible exceptions in conversions have already taken place.
            ConversionsToHastus.convertRouteDirection(routeIdentifier.direction)
        },
        Stop points: $stopLabels,
        Place codes: ${placeCodes.map { /* replace nulls with empty strings */ it ?: "" }}
        """.trimIndent()
    )
}
