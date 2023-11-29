package fi.hsl.jore4.hastus.service.importing

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ErrorWhileProcessingHastusDataException(
    reason: String
) : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, reason)
