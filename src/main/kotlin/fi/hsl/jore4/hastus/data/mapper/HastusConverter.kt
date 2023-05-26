package fi.hsl.jore4.hastus.data.mapper

import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.Place
import fi.hsl.jore4.hastus.data.hastus.Route
import fi.hsl.jore4.hastus.data.hastus.RouteVariant
import fi.hsl.jore4.hastus.data.hastus.RouteVariantPoint
import fi.hsl.jore4.hastus.data.hastus.Stop
import fi.hsl.jore4.hastus.data.jore.JoreHastusPlace
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop

object HastusConverter {

    fun convertJoreLinesToHastus(lines: List<JoreLine>): List<IHastusData> {
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

    fun convertJoreRoutesToRouteVariants(routes: List<JoreRoute>, routeLabel: String): List<IHastusData> {
        return routes.flatMap {
            val variant = it.variant.ifEmpty { it.direction.toString() }
            listOf(
                RouteVariant(
                    identifier = variant,
                    description = it.name,
                    direction = it.direction - 1,
                    reversible = it.reversible,
                    routeIdAndVariantId = it.label + variant,
                    routeId = routeLabel
                )
            ) + convertJoreRouteScheduledStopsToRouteVariantPoints(it.stopsOnRoute, it.label + variant)
        }
    }

    fun convertJoreRouteScheduledStopsToRouteVariantPoints(
        stopPoints: List<JoreRouteScheduledStop>,
        routeIdAndVariant: String
    ): List<RouteVariantPoint> {
        return stopPoints.map {
            RouteVariantPoint(
                place = it.hastusPlace,
                specTpDistance = NumberWithAccuracy(it.distanceToNextStop / 1000.0, 0, 3),
                isTimingPoint = it.isTimingPoint,
                allowLoadTime = it.isAllowedLoad,
                regulatedTp = it.isRegulatedTimingPoint,
                stopLabel = it.stopLabel,
                routeIdAndVariantId = routeIdAndVariant
            )
        }
    }

    fun convertJoreStopsToHastus(
        stops: List<JoreScheduledStop>
    ): List<Stop> {
        return stops
            .map {
                Stop(
                    identifier = it.label,
                    platform = it.platform,
                    descriptionFinnish = it.nameFinnish,
                    descriptionSwedish = it.nameSwedish,
                    streetFinnish = it.streetNameFinnish,
                    streetSwedish = it.streetNameSwedish,
                    place = it.hastusPlace,
                    gpsX = NumberWithAccuracy(it.location.x, 2, 6),
                    gpsY = NumberWithAccuracy(it.location.y, 2, 6),
                    shortIdentifier = it.label
                )
            }
    }

    fun convertJorePlacesToHastus(
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
}
