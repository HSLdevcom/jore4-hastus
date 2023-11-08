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
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreStopPointInJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace

object ConversionsToHastus {

    private const val MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION = 50
    private const val MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION = 60

    private const val MAX_LENGTH_HASTUS_STOP_NAME_IN_FINNISH = 100
    private const val MAX_LENGTH_HASTUS_STOP_NAME_IN_SWEDISH = 100

    private const val MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_FINNISH = 50
    private const val MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_SWEDISH = 50

    fun convertJoreLinesToHastus(lines: List<JoreLine>): List<IHastusData> {
        return lines.flatMap { joreLine ->

            // The cut is done due to length limit in Hastus.
            val hastusRouteDescription: String = joreLine.name.take(MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION)

            val hastusRoute = Route(
                identifier = joreLine.label,
                description = hastusRouteDescription,
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
            val hastusRouteVariantId: String =
                getHastusRouteVariantId(joreLineLabel, joreRoute.label, joreRoute.variant, joreRoute.direction)
            val hastusRouteIdAndVariantId: String = joreLineLabel + hastusRouteVariantId
            val hastusRouteVariantDirection: Int = getRouteDirectionAsNumberOrThrow(joreRoute.direction) - 1

            // The cut is done due to length limit in Hastus.
            val hastusRouteVariantDescription: String = joreRoute.name.take(MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION)

            val hastusRouteVariant = RouteVariant(
                identifier = hastusRouteVariantId,
                description = hastusRouteVariantDescription,
                direction = hastusRouteVariantDirection,
                reversible = joreRoute.reversible,
                routeIdAndVariantId = hastusRouteIdAndVariantId,
                routeId = joreLineLabel
            )

            val hastusRouteVariantPoints: List<RouteVariantPoint> =
                convertJoreStopPointsInJourneyPatternToHastusRouteVariantPoints(
                    joreRoute.stopPointsInJourneyPattern,
                    hastusRouteIdAndVariantId
                )

            listOf(hastusRouteVariant) + hastusRouteVariantPoints
        }
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

    fun convertJoreStopPointsToHastus(
        joreStopPoints: List<JoreScheduledStop>
    ): List<Stop> {
        return joreStopPoints.map {
            // The cuts below are done due to length limits in Hastus.

            val stopNameFi: String = it.nameFinnish.take(MAX_LENGTH_HASTUS_STOP_NAME_IN_FINNISH)
            val stopNameSv: String = it.nameSwedish.take(MAX_LENGTH_HASTUS_STOP_NAME_IN_SWEDISH)

            val stopStreetNameFi: String = it.streetNameFinnish.take(MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_FINNISH)
            val stopStreetNameSv: String = it.streetNameSwedish.take(MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_SWEDISH)

            Stop(
                identifier = it.label,
                platform = it.platform,
                descriptionFinnish = stopNameFi,
                descriptionSwedish = stopNameSv,
                streetFinnish = stopStreetNameFi,
                streetSwedish = stopStreetNameSv,
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
