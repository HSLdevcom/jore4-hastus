package fi.hsl.jore4.hastus.graphql

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.jackson.types.OptionalInput
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import fi.hsl.jore4.hastus.data.IHastusData
import fi.hsl.jore4.hastus.data.mapper.HastusMapper
import fi.hsl.jore4.hastus.generated.LinesWithHastusData
import fi.hsl.jore4.hastus.generated.RoutesWithHastusData
import fi.hsl.jore4.hastus.util.CsvWriter
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate

@Service
class GraphQLService(private val config: HasuraConfiguration) {
    val hasuraRole = "admin"

    val client = GraphQLKtorClient(
        httpClient = io.ktor.client.HttpClient() {
            defaultRequest {
                header("Content-type", "application/json; charset=UTF-8")
            }
        },
        url = URL(config.url),
        serializer = GraphQLClientJacksonSerializer()
    )

    fun getRoutesForLines(
        lines: List<String>,
        priority: Int,
        observationDate: LocalDate,
        authToken: String?,
        hasuraRole: String?,
        hasuraSecret: String?
    ): String {
        val query = LinesWithHastusData(
            variables = LinesWithHastusData.Variables(
                line_labels = OptionalInput.Defined(lines),
                line_priority = priority,
                observation_date = observationDate
            )
        )
        val result = runBlocking {
            client.execute(query) {
                if (authToken != null) header("auth", authToken)
                if (hasuraRole != null) header("x-hasura-role", hasuraRole)
                if (hasuraSecret != null) header("x-hasura-admin-secret", hasuraSecret)
            }.data
        }

        val routesAndVariants = HastusMapper.convertLines(result?.route_line.orEmpty())
        val stops = result?.route_line.orEmpty()
            .flatMap { it.line_routes }
            .flatMap { it.route_journey_patterns }
            .flatMap { it.scheduled_stop_point_in_journey_patterns }
            .flatMap { it.scheduled_stop_points }
        val hastusStops = HastusMapper.convertLineStops(stops).distinct()
        val places = stops.mapNotNull { it.timing_place }
        val hastusPlaces = HastusMapper.convertLinePlaces(places).distinct()

        val stopDistances: List<IHastusData> = emptyList()

        val allItems = routesAndVariants + hastusStops + hastusPlaces + stopDistances

        val writer = CsvWriter()

        return allItems.joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }

    fun getRoutesForRoutes(
        routes: List<String>,
        priority: Int,
        observationDate: LocalDate,
        authToken: String?,
        hasuraRole: String?,
        hasuraSecret: String?
    ): String {
        val query = RoutesWithHastusData(
            variables = RoutesWithHastusData.Variables(
                route_labels = OptionalInput.Defined(routes),
                route_priority = priority,
                observation_date = observationDate
            )
        )
        val result = runBlocking {
            client.execute(query) {
                if (authToken != null) header("auth", authToken)
                if (hasuraRole != null) header("x-hasura-role", hasuraRole)
                if (hasuraSecret != null) header("x-hasura-admin-secret", hasuraSecret)
            }.data
        }

        val routesAndVariants = HastusMapper.convertRoutes(result?.route_route.orEmpty())
        val stops = result?.route_route.orEmpty()
            .flatMap { it.route_journey_patterns }
            .flatMap { it.scheduled_stop_point_in_journey_patterns }
            .flatMap { it.scheduled_stop_points }
        val hastusStops = HastusMapper.convertRouteStops(stops).distinct()
        val places = stops.mapNotNull { it.timing_place }
        val hastusPlaces = HastusMapper.convertRoutePlaces(places).distinct()

        val stopDistances: List<IHastusData> = emptyList()

        val allItems = routesAndVariants + hastusStops + hastusPlaces + stopDistances

        val writer = CsvWriter()

        return allItems.joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }
}
