package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.format.Coordinate
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.Place
import fi.hsl.jore4.hastus.data.hastus.Stop
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreStopPointInJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.util.CsvWriter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

/**
 * This class contains integration tests for combined use of [ConversionsToHastus] and [CsvWriter].
 */
@DisplayName("Test conversions to CSV via Hastus types")
class ConversionsToCsvTest {

    @Nested
    @DisplayName("When transforming lines and routes")
    inner class WhenTransformingLinesAndRoutes {

        @Test
        fun `when the first and last stop are timing points`() {
            val expectedCsv = """
            route;65;Rautatientori - Veräjälaakso FI;0;0;0
            rvariant;1;Reitti A - B FI;0;0;65x1;65
            rvpoint;1AACKT;0.000;1;0;0;H1234;65x1
            rvpoint;;;0;0;0;H1235;65x1
            rvpoint;1AURLA;2.234;1;1;1;H1236;65x1
            rvpoint;;;0;0;0;H1237;65x1
            rvpoint;1KALA;0.750;1;0;0;H1238;65x1
            rvariant;2;Reitti A - B 3 FI;0;0;65y2;65
            rvpoint;1AACKT;0.000;1;0;0;H1234;65y2
            rvpoint;;;0;0;0;H1235;65y2
            rvpoint;1AURLA;2.234;1;1;1;H1236;65y2
            rvpoint;;;0;0;0;H1237;65y2
            rvpoint;1KALA;0.750;1;0;0;H1238;65y2
            """.trimIndent()

            val stopPointsInJourneyPattern = listOf(
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1234",
                    stopSequenceNumber = 1,
                    timingPlaceCode = "1AACKT",
                    isUsedAsTimingPoint = true,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 1234.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1235",
                    stopSequenceNumber = 2,
                    timingPlaceCode = "1ELIMK",
                    isUsedAsTimingPoint = false,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 1000.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1236",
                    stopSequenceNumber = 3,
                    timingPlaceCode = "1AURLA",
                    isUsedAsTimingPoint = true,
                    isRegulatedTimingPoint = true,
                    isAllowedLoad = true,
                    distanceToNextStop = 250.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1237",
                    stopSequenceNumber = 4,
                    timingPlaceCode = null,
                    isUsedAsTimingPoint = false,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 500.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1238",
                    stopSequenceNumber = 5,
                    timingPlaceCode = "1KALA",
                    isUsedAsTimingPoint = true,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 0.0
                )
            )

            val typeOfLine = "stopping_bus_service"

            val line = JoreLine(
                label = "65",
                name = "Rautatientori - Veräjälaakso FI",
                typeOfLine = typeOfLine,
                vehicleMode = 0,
                routes = listOf(
                    JoreRoute(
                        label = "65x",
                        variant = null,
                        name = "Reitti A - B FI",
                        direction = JoreRouteDirection.OUTBOUND,
                        reversible = false,
                        validityStart = LocalDate.of(2023, 1, 1),
                        validityEnd = LocalDate.of(2050, 12, 31),
                        typeOfLine = typeOfLine,
                        journeyPatternId = UUID.randomUUID(),
                        stopPointsInJourneyPattern = stopPointsInJourneyPattern
                    ),
                    JoreRoute(
                        label = "65y",
                        variant = "2",
                        name = "Reitti A - B 3 FI",
                        direction = JoreRouteDirection.OUTBOUND,
                        reversible = false,
                        validityStart = LocalDate.of(2023, 1, 1),
                        validityEnd = LocalDate.of(2050, 12, 31),
                        typeOfLine = typeOfLine,
                        journeyPatternId = UUID.randomUUID(),
                        stopPointsInJourneyPattern = stopPointsInJourneyPattern
                    )
                )
            )

            val hastusData: List<IHastusData> = ConversionsToHastus.convertJoreLinesToHastus(listOf(line))

            assertEquals(expectedCsv, CsvWriter().transformToCsv(hastusData))
        }

        @Test
        fun `when the first and last stop are NOT timing points`() {
            val expectedCsv = """
            route;65;Rautatientori - Veräjälaakso FI;0;0;0
            rvariant;1;Reitti A - B FI;0;0;65x1;65
            rvpoint;;;0;0;0;H1234;65x1
            rvpoint;1ELIMK;0.000;1;0;0;H1235;65x1
            rvpoint;1AURLA;1.000;1;0;1;H1236;65x1
            rvpoint;;;0;0;0;H1237;65x1
            """.trimIndent()

            val stopPointsInJourneyPattern = listOf(
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1234",
                    stopSequenceNumber = 1,
                    timingPlaceCode = "1AACKT",
                    isUsedAsTimingPoint = false,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 1234.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1235",
                    stopSequenceNumber = 2,
                    timingPlaceCode = "1ELIMK",
                    isUsedAsTimingPoint = true,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 1000.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1236",
                    stopSequenceNumber = 3,
                    timingPlaceCode = "1AURLA",
                    isUsedAsTimingPoint = true,
                    isRegulatedTimingPoint = true,
                    isAllowedLoad = false,
                    distanceToNextStop = 2500.0
                ),
                JoreStopPointInJourneyPattern(
                    stopLabel = "H1237",
                    stopSequenceNumber = 4,
                    timingPlaceCode = "1KALA",
                    isUsedAsTimingPoint = false,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    distanceToNextStop = 0.0
                )
            )

            val typeOfLine = "stopping_bus_service"

            val line = JoreLine(
                label = "65",
                name = "Rautatientori - Veräjälaakso FI",
                typeOfLine = typeOfLine,
                vehicleMode = 0,
                routes = listOf(
                    JoreRoute(
                        label = "65x",
                        variant = null,
                        name = "Reitti A - B FI",
                        direction = JoreRouteDirection.OUTBOUND,
                        reversible = false,
                        validityStart = LocalDate.of(2023, 1, 1),
                        validityEnd = LocalDate.of(2050, 12, 31),
                        typeOfLine = typeOfLine,
                        journeyPatternId = UUID.randomUUID(),
                        stopPointsInJourneyPattern = stopPointsInJourneyPattern
                    )
                )
            )

            val hastusData: List<IHastusData> = ConversionsToHastus.convertJoreLinesToHastus(listOf(line))

            assertEquals(expectedCsv, CsvWriter().transformToCsv(hastusData))
        }
    }

    @Test
    fun `when converting stops`() {
        val expectedCsv = """
            stop;H1234;00;kuvaus;beskrivning;katu;gata;1AACKT;24.928327;60.163918;H1234
            stop;H1235;00;kuvaus2;beskrivning2;katu2;gata2;1ELIMK;24.930490;60.164635;H1235
            stop;H1236;00;kuvaus3;beskrivning3;katu3;gata3;;24.931746;60.165123;H1236
            stop;H1237;00;kuvaus4;beskrivning4;katu4;gata4;1AURLA;24.933252;60.165655;H1237
        """.trimIndent()

        val stopPoints = listOf(
            JoreScheduledStop(
                label = "H1234",
                platform = "00",
                nameFinnish = "kuvaus",
                nameSwedish = "beskrivning",
                streetNameFinnish = "katu",
                streetNameSwedish = "gata",
                timingPlaceShortName = "1AACKT",
                location = Coordinate(24.928327, 60.163918)
            ),
            JoreScheduledStop(
                label = "H1235",
                platform = "00",
                nameFinnish = "kuvaus2",
                nameSwedish = "beskrivning2",
                streetNameFinnish = "katu2",
                streetNameSwedish = "gata2",
                timingPlaceShortName = "1ELIMK",
                location = Coordinate(24.930490, 60.164635)
            ),
            JoreScheduledStop(
                label = "H1236",
                platform = "00",
                nameFinnish = "kuvaus3",
                nameSwedish = "beskrivning3",
                streetNameFinnish = "katu3",
                streetNameSwedish = "gata3",
                timingPlaceShortName = null,
                location = Coordinate(24.931746, 60.165123)
            ),
            JoreScheduledStop(
                label = "H1237",
                platform = "00",
                nameFinnish = "kuvaus4",
                nameSwedish = "beskrivning4",
                streetNameFinnish = "katu4",
                streetNameSwedish = "gata4",
                timingPlaceShortName = "1AURLA",
                location = Coordinate(24.933252, 60.165655)
            )
        )

        val hastusStops: List<Stop> = ConversionsToHastus.convertJoreStopPointsToHastus(stopPoints)

        assertEquals(expectedCsv, CsvWriter().transformToCsv(hastusStops))
    }

    @Test
    fun `when converting timing places`() {
        val expectedCsv = """
            place;1AACKT;Aino Ackten tie
            place;1ELIMK;Elimäenkatu
        """.trimIndent()

        val timingPlaces = listOf(
            JoreTimingPlace(
                label = "1AACKT",
                description = "Aino Ackten tie"
            ),
            JoreTimingPlace(
                label = "1ELIMK",
                description = "Elimäenkatu"
            )
        )

        val hastusPlaces: List<Place> = ConversionsToHastus.convertJoreTimingPlacesToHastus(timingPlaces)

        assertEquals(expectedCsv, CsvWriter().transformToCsv(hastusPlaces))
    }
}
