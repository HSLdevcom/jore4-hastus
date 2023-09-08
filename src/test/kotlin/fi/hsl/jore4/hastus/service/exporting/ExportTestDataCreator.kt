package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop

interface ExportTestDataCreator {

    fun createLine(stopsOnRoute: List<JoreRouteScheduledStop>): JoreLine {
        return JoreLine(
            label = "65",
            "Rautatientori - Veräjälaakso FI",
            0,
            listOf(
                JoreRoute(
                    label = "65x",
                    variant = "",
                    uniqueLabel = "65x",
                    name = "Reitti A - B FI",
                    direction = 1,
                    reversible = false,
                    stopsOnRoute = stopsOnRoute
                )
            )
        )
    }

    fun createFirstStopPoint(
        timingPlaceShortName: String?,
        isTimingPoint: Boolean = true
    ): JoreRouteScheduledStop {
        return JoreRouteScheduledStop(
            timingPlaceShortName = timingPlaceShortName,
            distanceToNextStop = 123.0,
            isRegulatedTimingPoint = false,
            isAllowedLoad = false,
            isTimingPoint = isTimingPoint,
            stopLabel = "H1000"
        )
    }

    fun createLastStopPoint(
        timingPlaceShortName: String?,
        isTimingPoint: Boolean = true
    ): JoreRouteScheduledStop {
        return JoreRouteScheduledStop(
            timingPlaceShortName = timingPlaceShortName,
            distanceToNextStop = 0.0,
            isRegulatedTimingPoint = false,
            isAllowedLoad = false,
            isTimingPoint = isTimingPoint,
            stopLabel = "H9999"
        )
    }
}
