package fi.hsl.jore4.hastus.graphql

import com.expediagroup.graphql.client.jackson.GraphQLClientJacksonSerializer
import com.expediagroup.graphql.client.jackson.types.OptionalInput
import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.StopDistance
import fi.hsl.jore4.hastus.data.jore.JoreDistance
import fi.hsl.jore4.hastus.data.mapper.HastusConverter
import fi.hsl.jore4.hastus.generated.DistanceBetweenStopPoints
import fi.hsl.jore4.hastus.generated.RoutesWithHastusData
import fi.hsl.jore4.hastus.graphql.converter.ResultConverter
import fi.hsl.jore4.hastus.util.CsvWriter
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDate
import java.util.*

@Service
class GraphQLService(private val config: HasuraConfiguration) {

    private val LOGGER = KotlinLogging.logger {}

    private val client = GraphQLKtorClient(
        httpClient = io.ktor.client.HttpClient() {
            defaultRequest {
                header("Content-type", "application/json; charset=UTF-8")
            }
        },
        url = URL(config.url),
        serializer = GraphQLClientJacksonSerializer()
    )

    private val writer = CsvWriter()

    private fun convertRoutes(
        routes: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route>,
        distances: Map<Pair<String, String>, Int>
    ): List<IHastusData> {
        val dbLines = routes.mapNotNull { it.route_line }.distinctBy { it.label }
        val joreLines = dbLines.map {
            ResultConverter.mapJoreLine(it, routes.filter { r -> r.route_line?.label == it.label }, distances)
        }

        val stops = routes
            .flatMap { it.route_journey_patterns }
            .flatMap { it.scheduled_stop_point_in_journey_patterns }
            .flatMap { it.scheduled_stop_points }
            .distinct()

        val joreStops = stops.map { ResultConverter.mapJoreStop(it) }
        val jorePlaces = stops
            .mapNotNull { it.timing_place }
            .distinct()
            .map { ResultConverter.mapJoreHastusPlace(it) }

        return HastusConverter.convertJoreLinesToHastus(joreLines) +
            HastusConverter.convertJoreStopsToHastus(joreStops) +
            HastusConverter.convertJorePlacesToHastus(jorePlaces)
    }

    private fun convertRouteStops(stops: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point>): List<IHastusData> {
        return HastusConverter.convertJoreStopsToHastus(stops.map { ResultConverter.mapJoreStop(it) })
    }

    private fun convertRoutePlaces(places: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.timing_pattern_timing_place>): List<IHastusData> {
        return HastusConverter.convertJorePlacesToHastus(places.map { ResultConverter.mapJoreHastusPlace(it) })
    }

    private fun convertDistances(distances: List<JoreDistance>): List<StopDistance> {
        return distances.map {
            StopDistance(
                stopStart = it.startLabel,
                stopEnd = it.endLabel,
                editedDistance = it.distance
            )
        }
    }

    private fun getStopDistances(
        routes: List<UUID>,
        observationDate: LocalDate,
        cookieToken: String?,
        hasuraRole: String?
    ): List<JoreDistance> {
        val query = DistanceBetweenStopPoints(
            variables = DistanceBetweenStopPoints.Variables(
                routes = OptionalInput.Defined(UUIDList(routes)),
                observation_date = observationDate
            )
        )
        val result = runBlocking {
            val queryResponse = client.execute(query) {
                if (cookieToken != null) header("Cookie", cookieToken)
                if (hasuraRole != null) header("x-hasura-role", hasuraRole)
            }
            LOGGER.debug { "distance between stops graphQL response: $queryResponse" }
            if (queryResponse.errors?.isNotEmpty() == true) {
                throw IllegalStateException(queryResponse.errors?.toString())
            }
            queryResponse.data
        }

        val convertedResult = result?.service_pattern_get_distances_between_stop_points_by_routes?.map { ResultConverter.mapJoreDistance(it) }.orEmpty()

        return convertedResult.distinct()
    }

    fun getRoutes(
        uniqueRoutes: List<String>,
        priority: Int,
        observationDate: LocalDate,
        cookieToken: String?,
        hasuraRole: String?
    ): String {
        val query = RoutesWithHastusData(
            variables = RoutesWithHastusData.Variables(
                route_labels = OptionalInput.Defined(uniqueRoutes),
                route_priority = priority,
                observation_date = observationDate
            )
        )
        val result = runBlocking {
            val queryResponse = client.execute(query) {
                if (cookieToken != null) header("Cookie", cookieToken)
                if (hasuraRole != null) header("x-hasura-role", hasuraRole)
            }
            LOGGER.debug { "routes for routes graphQL response: $queryResponse" }
            if (queryResponse.errors?.isNotEmpty() == true) {
                throw IllegalStateException(queryResponse.errors?.toString())
            }
            queryResponse.data
        }

        val routeIds: List<UUID> = result?.route_route?.map { UUID.fromString(it.route_id) }.orEmpty()
        val distances: List<JoreDistance> = getStopDistances(
            routeIds,
            observationDate,
            cookieToken,
            hasuraRole
        )

        val distanceMap = distances.associate { Pair(it.startLabel, it.endLabel) to it.distance }

        val routesAndVariants = convertRoutes(result?.route_route.orEmpty(), distanceMap)
        val convertedDistances = convertDistances(distances)

        return (routesAndVariants + convertedDistances).distinct().joinToString(System.lineSeparator()) { writer.transformToCsvLine(it) }
    }
}
