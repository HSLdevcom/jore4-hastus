package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_STOP_NAME
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_STOP_STREET_NAME
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.NumberWithAccuracy
import fi.hsl.jore4.hastus.data.hastus.exp.IExportableItem
import fi.hsl.jore4.hastus.data.hastus.exp.Place
import fi.hsl.jore4.hastus.data.hastus.exp.Route
import fi.hsl.jore4.hastus.data.hastus.exp.RouteVariant
import fi.hsl.jore4.hastus.data.hastus.exp.RouteVariantPoint
import fi.hsl.jore4.hastus.data.hastus.exp.Stop
import fi.hsl.jore4.hastus.data.hastus.exp.StopDistance
import fi.hsl.jore4.hastus.data.jore.JoreDistanceBetweenTwoStopPoints
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreStopPointInJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace

object ConversionsToHastus {

    fun convertJoreLinesToHastus(lines: List<JoreLine>): List<IExportableItem> = lines.flatMap { joreLine ->
        listOf(
            convertJoreLineToHastus(joreLine)
        ).plus(
            convertJoreRoutesToHastusRouteVariants(joreLine.routes, joreLine.label)
        )
    }

    internal fun convertJoreLineToHastus(joreLine: JoreLine): Route {
        // The cut is done due to length limit in Hastus.
        val hastusRouteDescription: String = joreLine.name.take(MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION)

        return Route(
            identifier = joreLine.label,
            description = hastusRouteDescription,
            serviceType = 0,
            direction = 0,
            serviceMode = joreLine.vehicleMode
        )
    }

    private fun convertJoreRoutesToHastusRouteVariants(
        joreRoutes: List<JoreRoute>,
        joreLineLabel: String
    ): List<IExportableItem> {
        return joreRoutes.flatMap { joreRoute ->
            val hastusRouteVariant = convertJoreRouteToHastusRouteVariant(joreRoute, joreLineLabel)

            val hastusRouteVariantPoints: List<RouteVariantPoint> =
                convertJoreStopPointsInJourneyPatternToHastusRouteVariantPoints(
                    joreRoute.stopPointsInJourneyPattern,
                    hastusRouteVariant.routeIdAndVariantId
                )

            listOf(hastusRouteVariant) + hastusRouteVariantPoints
        }
    }

    internal fun convertJoreRouteToHastusRouteVariant(
        joreRoute: JoreRoute,
        joreLineLabel: String
    ): RouteVariant {
        val hastusRouteVariantId: String =
            getHastusRouteVariantId(joreLineLabel, joreRoute.label, joreRoute.variant, joreRoute.direction)
        val hastusRouteIdAndVariantId: String = joreLineLabel + hastusRouteVariantId
        val hastusRouteVariantDirection: Int = getRouteDirectionAsNumberOrThrow(joreRoute.direction) - 1

        // The cut is done due to length limit in Hastus.
        val hastusRouteVariantDescription: String = joreRoute.name.take(MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION)

        return RouteVariant(
            identifier = hastusRouteVariantId,
            description = hastusRouteVariantDescription,
            direction = hastusRouteVariantDirection,
            reversible = joreRoute.reversible,
            routeIdAndVariantId = hastusRouteIdAndVariantId,
            routeId = joreLineLabel
        )
    }

    internal fun getHastusRouteVariantId(
        joreLineLabel: String,
        joreRouteLabel: String,
        variant: String?,
        direction: JoreRouteDirection
    ): String {
        val majorVariant: String = joreRouteLabel.substringAfter(joreLineLabel)
        val minorVariant: String = variant ?: ""
        val routeDirection: Int = getRouteDirectionAsNumberOrThrow(direction)

        return "$majorVariant$minorVariant$routeDirection"
    }

    private fun getRouteDirectionAsNumberOrThrow(routeDirection: JoreRouteDirection): Int {
        return routeDirection.wellKnownNumber
            ?: throw IllegalArgumentException("Cannot convert Jore4 route direction to Hastus: $routeDirection")
    }

    private fun convertJoreStopPointsInJourneyPatternToHastusRouteVariantPoints(
        joreStopPointsInJourneyPattern: List<JoreStopPointInJourneyPattern>,
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

    fun convertJoreStopPointsToHastus(joreStopPoints: List<JoreScheduledStop>): List<Stop> =
        joreStopPoints.map(this::convertJoreStopPointToHastus)

    internal fun convertJoreStopPointToHastus(joreStopPoint: JoreScheduledStop): Stop {
        // The cuts below are done due to length limits in Hastus.

        val stopNameFi = joreStopPoint.nameFinnish.take(MAX_LENGTH_HASTUS_STOP_NAME)
        val stopNameSv = joreStopPoint.nameSwedish.take(MAX_LENGTH_HASTUS_STOP_NAME)

        val stopStreetNameFi = joreStopPoint.streetNameFinnish.take(MAX_LENGTH_HASTUS_STOP_STREET_NAME)
        val stopStreetNameSv = joreStopPoint.streetNameSwedish.take(MAX_LENGTH_HASTUS_STOP_STREET_NAME)

        return Stop(
            identifier = joreStopPoint.label,
            platform = joreStopPoint.platform,
            descriptionFinnish = stopNameFi,
            descriptionSwedish = stopNameSv,
            streetFinnish = stopStreetNameFi,
            streetSwedish = stopStreetNameSv,
            place = joreStopPoint.timingPlaceShortName,
            gpsX = NumberWithAccuracy(joreStopPoint.location.x, 2, 6),
            gpsY = NumberWithAccuracy(joreStopPoint.location.y, 2, 6),
            shortIdentifier = joreStopPoint.label
        )
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
