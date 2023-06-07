package fi.hsl.jore4.hastus.export

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class TooFewStopPointsException(routeLabel: String) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    "There are less than two stops points in the journey pattern for route $routeLabel"
)
