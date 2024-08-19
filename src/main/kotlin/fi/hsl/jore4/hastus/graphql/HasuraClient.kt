package fi.hsl.jore4.hastus.graphql

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import fi.hsl.jore4.hastus.graphql.converter.GraphQLAuthenticationFailedException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import java.net.URL

private val LOGGER = KotlinLogging.logger {}

/**
 * Hasura (GraphQL) client that provides logging facilities and exception handling on top of
 * [GraphQLKtorClient].
 *
 * @param[config] Configuration information used to set up GraphQL connections
 * @param[objectMapper] The JSON serializer used to print log messages
 */
class HasuraClient(
    config: HasuraConfiguration,
    private val objectMapper: ObjectMapper
) {
    private val gqlClient =
        GraphQLKtorClient(
            url = URL(config.url),
            httpClient =
                HttpClient {
                    defaultRequest {
                        contentType(ContentType.Application.Json.withParameter("charset", "utf-8"))
                    }
                    install(Logging) {
                        // Can be changed to HEADERS for debugging purposes
                        level = LogLevel.INFO
                    }
                },
            serializer = GraphQLClientJacksonSerializer()
        )

    fun <T : Any> sendRequest(
        request: GraphQLClientRequest<T>,
        httpHeaders: Map<String, String>
    ): T =
        runBlocking {
            LOGGER.debug {
                "GraphQL request:\n${request.query},\nvariables: ${request.variables}"
            }

            val queryResponse: GraphQLClientResponse<T> =
                gqlClient.execute(request) {
                    httpHeaders.map { header(it.key, it.value) }
                }

            LOGGER.debug {
                "GraphQL ${request.operationName} response: ${
                    objectMapper
                        // .writerWithDefaultPrettyPrinter() // disable commenting to get pretty-printed output
                        .writeValueAsString(queryResponse)
                }"
            }

            queryResponse.errors?.let { errorList ->
                if (errorList.isNotEmpty()) {
                    val authenticationFailedMessage: String? =
                        errorList
                            .find { error ->
                                // In the latter case (the "access-denied" code), there is also an
                                // error message, but checking the code is actually a more robust
                                // way to detect an authentication failure. In the former case, the
                                // code is "unexpected", from which the actual reason cannot be
                                // deduced.

                                "authentication request failed" in error.message ||
                                    error.extensions?.let { it["code"] == "access-denied" } ?: false
                            }?.message

                    if (authenticationFailedMessage != null) {
                        LOGGER.warn { "Authentication failed for GraphQL request" }
                        throw GraphQLAuthenticationFailedException(authenticationFailedMessage)
                    } else {
                        LOGGER.warn { "Got errors in GraphQL response: $errorList" }
                        throw IllegalStateException(errorList.toString())
                    }
                }
            }

            queryResponse.data
                ?: throw IllegalStateException("GraphQL response did not contain data even when no errors were present")
        }
}
