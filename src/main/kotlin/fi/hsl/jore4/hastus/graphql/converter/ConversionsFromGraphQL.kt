package fi.hsl.jore4.hastus.graphql.converter

import fi.hsl.jore4.hastus.Constants.LANG_FINNISH
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.journey_pattern_scheduled_stop_point_in_journey_pattern
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_line
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point

object ConversionsFromGraphQL {

    private fun convertVehicleMode(vehicleMode: String): Int = when (vehicleMode) {
        "train" -> 2
        else -> 0
    }

    fun mapToJoreLineAndRoutes(
        line: route_line,
        routesBelongingToLine: List<route_route>,
        distancesBetweenStops: Map<Pair<String, String>, Double>
    ): JoreLine {
        val lineName: String = line.name_i18n.content[LANG_FINNISH].orEmpty()

        return JoreLine(
            line.label,
            lineName,
            convertVehicleMode(line.vehicle_mode.vehicle_mode),
            routesBelongingToLine.map {
                mapToJoreRoute(it, distancesBetweenStops)
            }
        )
    }

    private fun mapToJoreRouteScheduledStop(
        stopInJourneyPattern: journey_pattern_scheduled_stop_point_in_journey_pattern?,
        distanceToNextStop: Double
    ): JoreRouteScheduledStop {
        if (stopInJourneyPattern == null) {
            throw IllegalStateException("Should not encounter a null journey pattern stop during conversion")
        }

        val stopLabel: String = stopInJourneyPattern.scheduled_stop_point_label

        val maxPriorityStopPoint: service_pattern_scheduled_stop_point = stopInJourneyPattern
            .scheduled_stop_points
            .maxByOrNull { it.priority }
            ?: run {
                // In this case, there is probably something wrong with the GraphQL query, because the
                // Jore4 database constraints should not allow this.
                throw IllegalStateException(
                    "Scheduled stop point (in journey pattern) not found for label: $stopLabel"
                )
            }

        return JoreRouteScheduledStop(
            stopLabel,
            maxPriorityStopPoint.timing_place?.label,
            stopInJourneyPattern.is_used_as_timing_point,
            stopInJourneyPattern.is_regulated_timing_point,
            stopInJourneyPattern.is_loading_time_allowed,
            distanceToNextStop
        )
    }

    private fun mapToJoreRoute(
        route: route_route,
        distancesBetweenStops: Map<Pair<String, String>, Double>
    ): JoreRoute {
        val routeName: String = route.name_i18n.content.getOrDefault(LANG_FINNISH, route.label)

        val journeyPatternStops = route.route_journey_patterns.flatMap { it.scheduled_stop_point_in_journey_patterns }

        // Add a null value to end so zipWithNext includes the last element as the last .first() element
        val journeyPatternStopsWithNextLabel: List<Pair<journey_pattern_scheduled_stop_point_in_journey_pattern, String?>> =
            (journeyPatternStops + null)
                .zipWithNext()
                .mapNotNull { (currentStop, nextStop) ->
                    currentStop?.let {
                        Pair(currentStop, nextStop?.scheduled_stop_point_label.orEmpty())
                    }
                }

        return JoreRoute(
            label = route.label,
            variant = route.variant,
            uniqueLabel = route.unique_label.orEmpty(),
            name = routeName,
            direction = JoreRouteDirection.from(route.direction),
            reversible = false,
            stopsOnRoute = journeyPatternStopsWithNextLabel.map { (journeyPatternStop, nextStopLabel) ->
                val currentStopLabel = journeyPatternStop.scheduled_stop_point_label
                val getDistanceKey = Pair(currentStopLabel, nextStopLabel)

                mapToJoreRouteScheduledStop(
                    journeyPatternStop,
                    distancesBetweenStops.getOrDefault(getDistanceKey, 0.0)
                )
            }
        )
    }
}
