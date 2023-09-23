package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UnmatchedRoutesWithinImport(routeIdentifiers: List<RouteLabelAndDirection>) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    "Could not find journey pattern for Hastus trips with the following route labels and directions: ${
        routeIdentifiers.joinToString(separator = ",")
    }"
)
