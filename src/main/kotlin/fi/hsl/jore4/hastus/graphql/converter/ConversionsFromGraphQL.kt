package fi.hsl.jore4.hastus.graphql.converter

import fi.hsl.jore4.hastus.Constants.LANG_FINNISH
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreStopPointInJourneyPattern
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.journey_pattern_journey_pattern
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

        // Currently, `typeOfLine` is modeled as an enumeration in the GraphQL API for the network
        // database but as a plain string in case of the timetables database. Therefore, we use
        // String type here to be compatible with the timetables API.
        val typeOfLine: String = line.type_of_line.name

        return JoreLine(
            line.label,
            lineName,
            typeOfLine,
            convertVehicleMode(line.vehicle_mode.vehicle_mode),
            routesBelongingToLine.map {
                mapToJoreRoute(it, typeOfLine, distancesBetweenStops)
            }
        )
    }

    private fun mapToJoreRouteScheduledStop(
        stopInJourneyPattern: journey_pattern_scheduled_stop_point_in_journey_pattern?,
        distanceToNextStop: Double
    ): JoreStopPointInJourneyPattern {
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

        return JoreStopPointInJourneyPattern(
            stopLabel,
            stopInJourneyPattern.scheduled_stop_point_sequence,
            maxPriorityStopPoint.timing_place?.label,
            stopInJourneyPattern.is_used_as_timing_point,
            stopInJourneyPattern.is_regulated_timing_point,
            stopInJourneyPattern.is_loading_time_allowed,
            distanceToNextStop
        )
    }

    private fun mapToJoreRoute(
        route: route_route,
        typeOfLine: String,
        distancesBetweenStops: Map<Pair<String, String>, Double>
    ): JoreRoute {
        val routeName: String = route.name_i18n.content.getOrDefault(LANG_FINNISH, route.label)

        // There is only one journey pattern per route in Jore4.
        val journeyPattern: journey_pattern_journey_pattern = route.route_journey_patterns.first()

        val journeyPatternStops: List<journey_pattern_scheduled_stop_point_in_journey_pattern> =
            journeyPattern.scheduled_stop_point_in_journey_patterns

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
            name = routeName,
            direction = JoreRouteDirection.from(route.direction),
            reversible = false,
            validityStart = route.validity_start,
            validityEnd = route.validity_end,
            typeOfLine = typeOfLine.lowercase(),
            journeyPatternId = journeyPattern.journey_pattern_id,
            stopPointsInJourneyPattern = journeyPatternStopsWithNextLabel.map { (journeyPatternStop, nextStopLabel) ->
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
