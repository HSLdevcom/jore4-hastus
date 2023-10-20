package fi.hsl.jore4.hastus.graphql.converter

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class GraphQLAuthenticationFailedException(message: String) : ResponseStatusException(
    HttpStatus.FORBIDDEN,
    message
)
