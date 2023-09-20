package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
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
                    variant = null,
                    uniqueLabel = "65x",
                    name = "Reitti A - B FI",
                    direction = JoreRouteDirection.OUTBOUND,
                    reversible = false,
                    stopsOnRoute = stopsOnRoute
                )
            )
        )
    }

    fun createFirstStopPoint(
        timingPlaceShortName: String?,
        isUsedAsTimingPoint: Boolean = true
    ): JoreRouteScheduledStop {
        return JoreRouteScheduledStop(
            stopLabel = "H1000",
            timingPlaceCode = timingPlaceShortName,
            isUsedAsTimingPoint = isUsedAsTimingPoint,
            isRegulatedTimingPoint = false,
            isAllowedLoad = false,
            distanceToNextStop = 123.0
        )
    }

    fun createLastStopPoint(
        timingPlaceShortName: String?,
        isUsedAsTimingPoint: Boolean = true
    ): JoreRouteScheduledStop {
        return JoreRouteScheduledStop(
            stopLabel = "H9999",
            timingPlaceCode = timingPlaceShortName,
            isUsedAsTimingPoint = isUsedAsTimingPoint,
            isRegulatedTimingPoint = false,
            isAllowedLoad = false,
            distanceToNextStop = 0.0
        )
    }
}
