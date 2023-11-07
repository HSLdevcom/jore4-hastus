package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.Place
import fi.hsl.jore4.hastus.data.hastus.Route
import fi.hsl.jore4.hastus.data.hastus.RouteVariant
import fi.hsl.jore4.hastus.data.hastus.RouteVariantPoint
import fi.hsl.jore4.hastus.data.hastus.Stop
import fi.hsl.jore4.hastus.data.hastus.StopDistance
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace

object ConversionsToHastus {

    fun convertJoreLinesToHastus(lines: List<JoreLine>): List<IHastusData> {
        return lines.flatMap { joreLine ->
            val hastusRoute = Route(
                identifier = joreLine.label,
                description = joreLine.name,
                serviceType = 0,
                direction = 0,
                serviceMode = joreLine.vehicleMode
            )

            listOf(hastusRoute) + convertJoreRoutesToHastusRouteVariants(joreLine.routes, joreLine.label)
        }
    }

    private fun convertJoreRoutesToHastusRouteVariants(
        joreRoutes: List<JoreRoute>,
        joreLineLabel: String
    ): List<IHastusData> {
        return joreRoutes.flatMap { joreRoute ->
            val routeDirection: Int = convertRouteDirection(joreRoute.direction)
            val hastusRouteVariantId: String =
                joreRoute.variant?.takeIf { it.isNotBlank() } ?: routeDirection.toString()
            val routeUniqueLabel: String = joreRoute.label + hastusRouteVariantId

            val routeVariant = RouteVariant(
                identifier = hastusRouteVariantId,
                description = joreRoute.name,
                direction = routeDirection - 1,
                reversible = joreRoute.reversible,
                routeIdAndVariantId = routeUniqueLabel,
                routeId = joreLineLabel
            )

            val routeVariantPoints: List<RouteVariantPoint> =
                convertJoreStopPointsInJourneyPatternToHastusRouteVariantPoints(
                    joreRoute.stopsOnRoute,
                    routeUniqueLabel
                )

            listOf(routeVariant) + routeVariantPoints
        }
    }

    fun convertRouteDirection(routeDirection: JoreRouteDirection): Int = when (routeDirection) {
        JoreRouteDirection.OUTBOUND -> 1
        JoreRouteDirection.INBOUND -> 2
        else -> throw IllegalArgumentException("Cannot convert Jore4 route direction to Hastus: $routeDirection")
    }

    private fun convertJoreStopPointsInJourneyPatternToHastusRouteVariantPoints(
        joreStopPointsInJourneyPattern: List<JoreRouteScheduledStop>,
        hastusRouteIdAndVariantId: String
    ): List<RouteVariantPoint> {
        var firstTimingPointEncountered = false
        var accumulatedDistanceFromPreviousTimingPoint = 0.0

        // Regarding the distances between the stops, the following transformations are made in
        // relation to the input:
        // - Distances are given only for stops that are used as timing points.
        // - In the input, the distances are given as distances to the next stop. The distances are
        //   converted in such a way that we calculate for each timing point the distance from the
        //   previous timing point.
        return joreStopPointsInJourneyPattern.map { stop ->
            val specTpDistance: NumberWithAccuracy? = if (stop.isUsedAsTimingPoint) {
                val distanceFromPreviousTimingPoint = accumulatedDistanceFromPreviousTimingPoint

                firstTimingPointEncountered = true
                accumulatedDistanceFromPreviousTimingPoint = stop.distanceToNextStop

                NumberWithAccuracy(distanceFromPreviousTimingPoint / 1000.0, 1, 3)
            } else {
                if (firstTimingPointEncountered) {
                    accumulatedDistanceFromPreviousTimingPoint += stop.distanceToNextStop
                }

                // This is not timing point, so no distance is given.
                null
            }

            RouteVariantPoint(
                place = stop.effectiveTimingPlaceCode,
                specTpDistance = specTpDistance,
                isTimingPoint = stop.isUsedAsTimingPoint,
                allowLoadTime = stop.isAllowedLoad,
                regulatedTp = stop.isRegulatedTimingPoint,
                stopLabel = stop.stopLabel,
                routeIdAndVariantId = hastusRouteIdAndVariantId
            )
        }
    }

    fun convertJoreStopPointsToHastus(
        joreStopPoints: List<JoreScheduledStop>
    ): List<Stop> {
        return joreStopPoints.map {
            Stop(
                identifier = it.label,
                platform = it.platform,
                descriptionFinnish = it.nameFinnish,
                descriptionSwedish = it.nameSwedish,
                streetFinnish = it.streetNameFinnish,
                streetSwedish = it.streetNameSwedish,
                place = it.timingPlaceShortName,
                gpsX = NumberWithAccuracy(it.location.x, 2, 6),
                gpsY = NumberWithAccuracy(it.location.y, 2, 6),
                shortIdentifier = it.label
            )
        }
    }

    fun convertJoreTimingPlacesToHastus(
        joreTimingPlaces: List<JoreTimingPlace>
    ): List<Place> {
        return joreTimingPlaces.map {
            Place(
                identifier = it.label,
                description = it.description
            )
        }
    }

    fun convertDistancesBetweenStopPointsToHastus(
        distancesBetweenStopPoints: List<JoreDistanceBetweenTwoStopPoints>
    ): List<StopDistance> {
        return distancesBetweenStopPoints.map {
            StopDistance(
                stopStart = it.startLabel,
                stopEnd = it.endLabel,
                editedDistance = it.distance.toInt()
            )
        }
    }
}
