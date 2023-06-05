package fi.hsl.jore4.hastus.graphql.converter

import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.generated.distancebetweenstoppoints.service_pattern_distance_between_stops_calculation
import fi.hsl.jore4.hastus.generated.enums.route_direction_enum
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.journey_pattern_scheduled_stop_point_in_journey_pattern
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_line
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point
import fi.hsl.jore4.hastus.generated.routeswithhastusdata.timing_pattern_timing_place

object ConversionsFromGraphQL {

    private const val LANG_FINNISH = "fi_FI"
    private const val LANG_SWEDISH = "se_SE"

    private fun convertVehicleMode(vehicleMode: String): Int = when (vehicleMode) {
        "train" -> 2
        else -> 0
    }

    // 1 = outbound, 2 = inbound
    private fun convertRouteDirection(routeDirection: route_direction_enum): Int = when (routeDirection) {
        route_direction_enum.OUTBOUND -> 1
        route_direction_enum.INBOUND -> 2
        else -> throw IllegalArgumentException("Unsupported route direction: $routeDirection")
    }

    fun mapToJoreTimingPlace(timingPlace: timing_pattern_timing_place): JoreTimingPlace {
        val description: String = timingPlace
            .description
            ?.content
            ?.get(LANG_FINNISH) ?: timingPlace.label // Use label as description if one is not provided

        return JoreTimingPlace(timingPlace.label, description)
    }

    fun mapToJoreLineAndRoutes(
        line: route_line,
        routes: List<route_route>,
        distancesBetweenStops: Map<Pair<String, String>, Double>
    ): JoreLine {
        return JoreLine(
            line.label,
            line.name_i18n.content[LANG_FINNISH].orEmpty(),
            convertVehicleMode(line.vehicle_mode.vehicle_mode),
            routes.map { mapToJoreRoute(it, distancesBetweenStops) }
        )
    }

    fun mapToJoreStop(stop: service_pattern_scheduled_stop_point) =
        JoreScheduledStop(
            stop.label,
            "00", // TODO
            "kuvaus", // TODO
            "beskrivning", // TODO
            "katu", // TODO
            "gata", // TODO
            stop.timing_place?.label,
            stop.measured_location
        )

    private fun mapToJoreRouteScheduledStop(
        stopInJourneyPattern: journey_pattern_scheduled_stop_point_in_journey_pattern?,
        distanceToNextStop: Double
    ): JoreRouteScheduledStop {
        if (stopInJourneyPattern == null) {
            throw IllegalStateException("Should not be possible to get a null route stop when mapping")
        }

        return JoreRouteScheduledStop(
            stopInJourneyPattern.scheduled_stop_points.first().timing_place?.label,
            distanceToNextStop,
            stopInJourneyPattern.is_regulated_timing_point,
            stopInJourneyPattern.is_loading_time_allowed,
            stopInJourneyPattern.is_used_as_timing_point,
            stopInJourneyPattern.scheduled_stop_point_label
        )
    }

    private fun mapToJoreRoute(
        route: route_route,
        distancesBetweenStops: Map<Pair<String, String>, Double>
    ): JoreRoute {
        val stops = route.route_journey_patterns.flatMap { it.scheduled_stop_point_in_journey_patterns }

        // Add a null value to end so zipWithNext includes the last element as the last .first() element
        val stopsWithNextLabel = (stops + null).zipWithNext().map {
            Pair(it.first, it.second?.scheduled_stop_point_label.orEmpty())
        }

        return JoreRoute(
            label = route.label,
            variant = route.variant.orEmpty(),
            uniqueLabel = route.unique_label.orEmpty(),
            name = route.name_i18n.content.getOrDefault(LANG_FINNISH, route.label),
            direction = convertRouteDirection(route.direction),
            reversible = false,
            stopsOnRoute = stopsWithNextLabel.map {
                mapToJoreRouteScheduledStop(
                    it.first,
                    distancesBetweenStops.getOrDefault(Pair(it.first?.scheduled_stop_point_label, it.second), 0.0)
                )
            }
        )
    }

    fun mapToJoreDistance(distance: service_pattern_distance_between_stops_calculation) =
        JoreDistanceBetweenTwoStopPoints(
            distance.start_stop_label,
            distance.end_stop_label,
            distance.distance_in_metres.toDouble() // transform decimal number from String format to Double
        )
}
