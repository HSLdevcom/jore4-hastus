package fi.hsl.jore4.hastus.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import org.springframework.stereotype.Component

@Component
class GraphQLServiceFactory(
    hasuraConfig: HasuraConfiguration,
    objectMapper: ObjectMapper
) {
    private val hasuraClient = HasuraClient(hasuraConfig, objectMapper)

    /**
     * Creates a [GraphQLService] instance for one HTTP(S) session, i.e. single request-response
     * cycle.
     *
     * @param[httpHeaders] HTTP headers to be added to GraphQL requests
     */
    fun createForSession(httpHeaders: Map<String, String>) = GraphQLService(hasuraClient, httpHeaders)
}
