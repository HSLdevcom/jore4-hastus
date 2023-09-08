package fi.hsl.jore4.hastus.service.exporting.validation

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class FirstStopNotTimingPointException(routeLabel: String) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    "The first stop point in the journey pattern for route $routeLabel is not a timing point"
)
