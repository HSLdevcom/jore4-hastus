package fi.hsl.jore4.hastus.data.mapper

import fi.hsl.jore4.hastus.data.IHastusData
import fi.hsl.jore4.hastus.data.Place
import fi.hsl.jore4.hastus.data.Route
import fi.hsl.jore4.hastus.data.RouteVariant
import fi.hsl.jore4.hastus.data.RouteVariantPoint
import fi.hsl.jore4.hastus.data.Stop
import fi.hsl.jore4.hastus.generated.enums.route_direction_enum
import fi.hsl.jore4.hastus.graphql.Coordinate

class HastusMapper {

    companion object {
        private const val LANG_FINNISH = "fi_FI"
        private const val LANG_SWEDISH = "se_SE"

        data class JoreHastusPlace(val label: String, val description: String)

        data class JoreScheduledStop(
            val label: String,
            val platform: String,
            val name: HashMap<String, String>,
            val streetName: HashMap<String, String>,
            val hastusPlace: String,
            val location: Coordinate
        )

        data class JoreRouteScheduledStop(
            val hastusPlace: String,
            val distance: Double,
            val isInUse: Boolean,
            val isAllowedLoad: Boolean,
            val isTimingPoint: Boolean,
            val stopLabel: String
        )

        data class JoreRoute(
            val label: String,
            val name: String,
            val direction: Int,
            val stopsOnRoute: List<JoreRouteScheduledStop>
        )

        data class JoreLine(
            val label: String,
            val name: String,
            val vehicleMode: Int,
            val routes: List<JoreRoute>
        )

        private fun convertVehicleMode(vehicleMode: String): Int {
            return if (vehicleMode == "train") 2 else 0
        }

        // 1 = outbound, 2 = inbound
        private fun convertRouteDirection(direction: route_direction_enum): Int {
            return when (direction) {
                route_direction_enum.OUTBOUND -> 1
                route_direction_enum.INBOUND -> 2
                else -> 0
            }
        }

        private fun convertJoreLinesToHastus(lines: List<JoreLine>): List<IHastusData> {
            return lines.flatMap {
                listOf(
                    Route(
                        identifier = it.label,
                        description = it.name,
                        serviceType = 0,
                        direction = 0,
                        serviceMode = it.vehicleMode
                    )
                ) + convertJoreRoutesToRouteVariants(it.routes, it.label)
            }
        }

        private fun convertJoreRoutesToRouteVariants(routes: List<JoreRoute>, routeLabel: String): List<IHastusData> {
            return routes.flatMap { it ->
                listOf(
                    RouteVariant(
                        identifier = it.label,
                        description = it.name,
                        direction = it.direction,
                        reversible = false,
                        routeIdAndVariantId = routeLabel + it.label,
                        routeId = routeLabel
                    )
                ) + convertJoreRouteScheduledStopsToRouteVariantPoints(it.stopsOnRoute, routeLabel + it.label)
            }
        }

        private fun convertJoreRouteScheduledStopsToRouteVariantPoints(
            stopPoints: List<JoreRouteScheduledStop>,
            routeIdAndVariant: String
        ): List<RouteVariantPoint> {
            return stopPoints.map {
                RouteVariantPoint(
                    place = "place",
                    specTpDistance = 1234.0,
                    isTimingPoint = it.isInUse, // is in use
                    allowLoadTime = it.isAllowedLoad,
                    regulatedTp = it.isTimingPoint, // is a timing point
                    stopLabel = it.hastusPlace,
                    routeIdAndVariantId = routeIdAndVariant
                )
            }
        }

        private fun convertJoreStopsToHastus(
            stops: List<JoreScheduledStop>
        ): List<Stop> {
            return stops
                .map {
                    Stop(
                        identifier = it.label,
                        platform = it.platform,
                        descriptionFinnish = it.name.getOrDefault(LANG_FINNISH, ""),
                        descriptionSwedish = it.name.getOrDefault(LANG_SWEDISH, ""),
                        streetFinnish = it.streetName.getOrDefault(LANG_FINNISH, ""),
                        streetSwedish = it.streetName.getOrDefault(LANG_SWEDISH, ""),
                        place = it.hastusPlace,
                        gpsX = it.location.x,
                        gpsY = it.location.y,
                        shortIdentifier = it.label
                    )
                }
        }

        private fun convertJorePlacesToHastus(
            places: List<JoreHastusPlace>
        ): List<Place> {
            return places
                .map {
                    Place(
                        identifier = it.label,
                        description = it.description
                    )
                }
        }

        private fun mapJoreHastusPlace(hastusPlace: fi.hsl.jore4.hastus.generated.lineswithhastusdata.timing_pattern_timing_place): JoreHastusPlace {
            return JoreHastusPlace(
                hastusPlace.label,
                hastusPlace.description?.get(LANG_FINNISH) ?: hastusPlace.label // Use label as description if one is not provided
            )
        }

        private fun mapJoreHastusPlace(hastusPlace: fi.hsl.jore4.hastus.generated.routeswithhastusdata.timing_pattern_timing_place): JoreHastusPlace {
            return JoreHastusPlace(
                hastusPlace.label,
                hastusPlace.description?.get(LANG_FINNISH) ?: hastusPlace.label // Use label as description if one is not provided
            )
        }

        private fun mapJoreStop(stop: fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point): JoreScheduledStop {
            return JoreScheduledStop(
                stop.label,
                "00", // TODO
                hashMapOf(LANG_FINNISH to "kuvaus", LANG_SWEDISH to "beskrivning"),
                hashMapOf(LANG_FINNISH to "katu", LANG_SWEDISH to "gata"),
                stop.timing_place?.label.orEmpty(),
                stop.measured_location
            )
        }

        fun mapJoreStop(stop: fi.hsl.jore4.hastus.generated.lineswithhastusdata.service_pattern_scheduled_stop_point): JoreScheduledStop {
            return JoreScheduledStop(
                stop.label,
                "00", // TODO
                hashMapOf(LANG_FINNISH to "kuvaus", LANG_SWEDISH to "beskrivning"),
                hashMapOf(LANG_FINNISH to "katu", LANG_SWEDISH to "gata"),
                stop.timing_place?.label.orEmpty(),
                stop.measured_location
            )
        }

        private fun mapJoreRouteScheduledStop(routeStop: fi.hsl.jore4.hastus.generated.routeswithhastusdata.journey_pattern_scheduled_stop_point_in_journey_pattern): JoreRouteScheduledStop {
            return JoreRouteScheduledStop(
                routeStop.scheduled_stop_points.first().timing_place?.label.orEmpty(),
                1234.0, // TODO: Distance
                routeStop.scheduled_stop_points.first().timing_place != null, // TODO: Hastus place is in use if it is defined. Replace with boolean value once it is added to data model
                false, // TODO: Is waiting for loading allowed
                routeStop.is_timing_point,
                routeStop.scheduled_stop_point_label
            )
        }

        private fun mapJoreRouteScheduledStop(routeStop: fi.hsl.jore4.hastus.generated.lineswithhastusdata.journey_pattern_scheduled_stop_point_in_journey_pattern): JoreRouteScheduledStop {
            return JoreRouteScheduledStop(
                routeStop.scheduled_stop_points.first().timing_place?.label.orEmpty(),
                1234.0, // TODO: Distance
                routeStop.scheduled_stop_points.first().timing_place != null, // TODO: Hastus place is in use if it is defined. Replace with boolean value once it is added to data model
                false, // TODO: Is waiting for loading allowed
                routeStop.is_timing_point,
                routeStop.scheduled_stop_point_label
            )
        }

        private fun mapJoreRoute(routeRoute: fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route): JoreRoute {
            return JoreRoute(
                routeRoute.label,
                routeRoute.name_i18n.getOrDefault(LANG_FINNISH, routeRoute.label),
                convertRouteDirection(routeRoute.direction),
                routeRoute.route_journey_patterns.flatMap { it.scheduled_stop_point_in_journey_patterns }.map { mapJoreRouteScheduledStop(it) }
            )
        }

        private fun mapJoreRoute(routeRoute: fi.hsl.jore4.hastus.generated.lineswithhastusdata.route_route): JoreRoute {
            return JoreRoute(
                routeRoute.label,
                routeRoute.name_i18n.getOrDefault(LANG_FINNISH, routeRoute.label),
                convertRouteDirection(routeRoute.direction),
                routeRoute.route_journey_patterns.flatMap { it.scheduled_stop_point_in_journey_patterns }.map { mapJoreRouteScheduledStop(it) }
            )
        }

        private fun mapJoreLine(routeLine: fi.hsl.jore4.hastus.generated.lineswithhastusdata.route_line): JoreLine {
            return JoreLine(
                routeLine.label,
                routeLine.name_i18n[LANG_FINNISH].orEmpty(),
                convertVehicleMode(routeLine.vehicle_mode.vehicle_mode),
                routeLine.line_routes.map { mapJoreRoute(it) }
            )
        }

        private fun mapJoreLine(
            routeLine: fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_line,
            routes: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route>
        ): JoreLine {
            return JoreLine(
                routeLine.label,
                routeLine.name_i18n[LANG_FINNISH].orEmpty(),
                convertVehicleMode(routeLine.vehicle_mode.vehicle_mode),
                routes.map { mapJoreRoute(it) }
            )
        }

        fun convertLines(lines: List<fi.hsl.jore4.hastus.generated.lineswithhastusdata.route_line>): List<IHastusData> {
            val joreLines = lines.map { mapJoreLine(it) }

            return convertJoreLinesToHastus(joreLines)
        }

        fun convertLineStops(stops: List<fi.hsl.jore4.hastus.generated.lineswithhastusdata.service_pattern_scheduled_stop_point>): List<IHastusData> {
            return convertJoreStopsToHastus(stops.map { mapJoreStop(it) })
        }

        fun convertLinePlaces(places: List<fi.hsl.jore4.hastus.generated.lineswithhastusdata.timing_pattern_timing_place>): List<IHastusData> {
            return convertJorePlacesToHastus(places.map { mapJoreHastusPlace(it) })
        }

        fun convertRoutes(routes: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.route_route>): List<IHastusData> {
            val dbLines = routes.mapNotNull { it.route_line }.distinctBy { it.label }
            val joreLines = dbLines.map { mapJoreLine(it, routes.filter { r -> r.route_line?.label == it.label }) }

            val stops = routes
                .flatMap { it.route_journey_patterns }
                .flatMap { it.scheduled_stop_point_in_journey_patterns }
                .flatMap { it.scheduled_stop_points }
                .distinct()

            val joreStops = stops.map { mapJoreStop(it) }
            val jorePlaces = stops
                .mapNotNull { it.timing_place }
                .distinct()
                .map { mapJoreHastusPlace(it) }

            return convertJoreLinesToHastus(joreLines) +
                convertJoreStopsToHastus(joreStops) +
                convertJorePlacesToHastus(jorePlaces)
        }

        fun convertRouteStops(stops: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.service_pattern_scheduled_stop_point>): List<IHastusData> {
            return convertJoreStopsToHastus(stops.map { mapJoreStop(it) })
        }

        fun convertRoutePlaces(places: List<fi.hsl.jore4.hastus.generated.routeswithhastusdata.timing_pattern_timing_place>): List<IHastusData> {
            return convertJorePlacesToHastus(places.map { mapJoreHastusPlace(it) })
        }
    }
}
