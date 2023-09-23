package fi.hsl.jore4.hastus.service.importing

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidHastusDataException(message: String) : ResponseStatusException(
    HttpStatus.BAD_REQUEST,
    message
)
