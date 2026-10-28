package fi.hsl.jore4.hastus.graphql

import fi.hsl.jore4.hastus.config.HasuraConfiguration
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class GraphQLServiceFactory(
    hasuraConfig: HasuraConfiguration,
    jsonMapper: JsonMapper
) {
    private val hasuraClient = HasuraClient(hasuraConfig, jsonMapper)

    /**
     * Creates a [GraphQLService] instance for one HTTP(S) session, i.e. single request-response
     * cycle.
     *
     * @param[httpHeaders] HTTP headers to be added to GraphQL requests
     */
    fun createForSession(httpHeaders: Map<String, String>) = GraphQLService(hasuraClient, httpHeaders)
}
