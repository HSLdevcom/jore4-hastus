package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreStopPointInJourneyPattern
import java.time.LocalDate
import java.util.UUID

interface ExportTestDataCreator {
    fun createLine(stopPointsInJourneyPattern: List<JoreStopPointInJourneyPattern>): JoreLine {
        val typeOfLine = "stopping_bus_service"

        return JoreLine(
            label = "65",
            "Rautatientori - Veräjälaakso FI",
            typeOfLine,
            0,
            listOf(
                JoreRoute(
                    label = "65x",
                    variant = null,
                    name = "Reitti A - B FI",
                    typeOfLine = typeOfLine,
                    direction = JoreRouteDirection.OUTBOUND,
                    reversible = false,
                    validityStart = LocalDate.of(2023, 1, 1),
                    validityEnd = LocalDate.of(2050, 12, 31),
                    journeyPatternId = UUID.randomUUID(),
                    stopPointsInJourneyPattern = stopPointsInJourneyPattern
                )
            )
        )
    }

    fun createFirstStopPoint(
        timingPlaceShortName: String?,
        isUsedAsTimingPoint: Boolean = true
    ): JoreStopPointInJourneyPattern {
        return JoreStopPointInJourneyPattern(
            stopLabel = "H1000",
            stopSequenceNumber = 1,
            timingPlaceCode = timingPlaceShortName,
            isUsedAsTimingPoint = isUsedAsTimingPoint,
            isRegulatedTimingPoint = false,
            isAllowedLoad = false,
            distanceToNextStop = 123.0
        )
    }

    fun createLastStopPoint(
        stopSequenceNumber: Int,
        timingPlaceShortName: String?,
        isUsedAsTimingPoint: Boolean = true
    ): JoreStopPointInJourneyPattern {
        return JoreStopPointInJourneyPattern(
            stopLabel = "H9999",
            stopSequenceNumber = stopSequenceNumber,
            timingPlaceCode = timingPlaceShortName,
            isUsedAsTimingPoint = isUsedAsTimingPoint,
            isRegulatedTimingPoint = false,
            isAllowedLoad = false,
            distanceToNextStop = 0.0
        )
    }
}
