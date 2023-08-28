package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class NoJourneyPatternRefMatchesHastusTripStopsException(message: String) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    message
) {
    constructor(routeIdentifier: RouteLabelAndDirection) : this(
        "No journey pattern reference was found whose stop points correspond to the Hastus trip: $routeIdentifier"
    )
}
