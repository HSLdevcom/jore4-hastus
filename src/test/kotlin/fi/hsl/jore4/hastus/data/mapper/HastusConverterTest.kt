package fi.hsl.jore4.hastus.data.mapper

import fi.hsl.jore4.hastus.data.format.Coordinate
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreTimingPlace
import fi.hsl.jore4.hastus.util.CsvWriter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Test the Hastus converter")
class HastusConverterTest {

    @Nested
    @DisplayName("When transforming lines and routes")
    inner class WhenTransformingLinesAndRoutes {

        @Test
        @DisplayName("When the first and last stop are timing points")
        fun whenFirstAndLastStopAreTimingPoints() {
            val expectedResult = """
            route;65;Rautatientori - Veräjälaakso FI;0;0;0
            rvariant;1;Reitti A - B FI;0;0;65x1;65
            rvpoint;1AACKT;0.000;1;0;0;H1234;65x1
            rvpoint;1ELIMK;;0;0;0;H1235;65x1
            rvpoint;1AURLA;2.234;1;1;1;H1236;65x1
            rvpoint;1KALA;0.250;1;0;0;H1237;65x1
            rvariant;2;Reitti A - B 3 FI;0;0;65y2;65
            rvpoint;1AACKT;0.000;1;0;0;H1234;65y2
            rvpoint;1ELIMK;;0;0;0;H1235;65y2
            rvpoint;1AURLA;2.234;1;1;1;H1236;65y2
            rvpoint;1KALA;0.250;1;0;0;H1237;65y2
            """.trimIndent()

            val joreStops = listOf(
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1AACKT",
                    distanceToNextStop = 1234.0,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    isTimingPoint = true,
                    stopLabel = "H1234"
                ),
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1ELIMK",
                    distanceToNextStop = 1000.0,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    isTimingPoint = false,
                    stopLabel = "H1235"
                ),
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1AURLA",
                    distanceToNextStop = 250.0,
                    isRegulatedTimingPoint = true,
                    isAllowedLoad = true,
                    isTimingPoint = true,
                    stopLabel = "H1236"
                ),
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1KALA",
                    distanceToNextStop = 0.0,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    isTimingPoint = true,
                    stopLabel = "H1237"
                )
            )
            val joreRoutes = listOf(
                JoreRoute("65x", "", "65x", "Reitti A - B FI", 1, false, joreStops),
                JoreRoute("65y", "2", "65y2", "Reitti A - B 3 FI", 1, false, joreStops)
            )
            val joreLine = JoreLine(
                label = "65",
                "Rautatientori - Veräjälaakso FI",
                0,
                joreRoutes
            )
            val testable = HastusConverter.convertJoreLinesToHastus(listOf(joreLine))

            val writer = CsvWriter()

            assertEquals(expectedResult, writer.transformToCsv(testable))
        }

        @Test
        @DisplayName("When the first and last stop are NOT timing points")
        fun whenFirstAndLastStopAreNotTimingPoints() {
            val expectedResult = """
            route;65;Rautatientori - Veräjälaakso FI;0;0;0
            rvariant;1;Reitti A - B FI;0;0;65x1;65
            rvpoint;1AACKT;;0;0;0;H1234;65x1
            rvpoint;1ELIMK;0.000;1;0;0;H1235;65x1
            rvpoint;1AURLA;1.000;1;0;1;H1236;65x1
            rvpoint;1KALA;;0;0;0;H1237;65x1
            """.trimIndent()

            val joreStops = listOf(
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1AACKT",
                    distanceToNextStop = 1234.0,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    isTimingPoint = false,
                    stopLabel = "H1234"
                ),
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1ELIMK",
                    distanceToNextStop = 1000.0,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    isTimingPoint = true,
                    stopLabel = "H1235"
                ),
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1AURLA",
                    distanceToNextStop = 2500.0,
                    isRegulatedTimingPoint = true,
                    isAllowedLoad = false,
                    isTimingPoint = true,
                    stopLabel = "H1236"
                ),
                JoreRouteScheduledStop(
                    timingPlaceShortName = "1KALA",
                    distanceToNextStop = 0.0,
                    isRegulatedTimingPoint = false,
                    isAllowedLoad = false,
                    isTimingPoint = false,
                    stopLabel = "H1237"
                )
            )
            val joreRoutes = listOf(
                JoreRoute("65x", "", "65x", "Reitti A - B FI", 1, false, joreStops)
            )
            val joreLine = JoreLine(
                label = "65",
                "Rautatientori - Veräjälaakso FI",
                0,
                joreRoutes
            )
            val testable = HastusConverter.convertJoreLinesToHastus(listOf(joreLine))

            val writer = CsvWriter()

            assertEquals(expectedResult, writer.transformToCsv(testable))
        }
    }

    @Test
    @DisplayName("When converting stops")
    fun whenMappingStops() {
        val expectedResult = """
            stop;H1234;00;kuvaus;beskrivning;katu;gata;1AACKT;24.928327;60.163918;H1234
            stop;H1235;00;kuvaus2;beskrivning2;katu2;gata2;1ELIMK;24.930490;60.164635;H1235
            stop;H1236;00;kuvaus3;beskrivning3;katu3;gata3;1AURLA;24.933252;60.165655;H1236
        """.trimIndent()

        val joreStops = listOf(
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
                timingPlaceShortName = "1AURLA",
                location = Coordinate(24.933252, 60.165655)
            )
        )
        val testable = HastusConverter.convertJoreStopsToHastus(joreStops)

        val writer = CsvWriter()

        assertEquals(expectedResult, writer.transformToCsv(testable))
    }

    @Test
    @DisplayName("When converting timing places")
    fun whenMappingTimingPlaces() {
        val expectedResult = """
            place;1AACKT;Aino Ackten tie
            place;1ELIMK;Elimäenkatu
        """.trimIndent()

        val joreTimingPlaces = listOf(
            JoreTimingPlace(
                label = "1AACKT",
                description = "Aino Ackten tie"
            ),
            JoreTimingPlace(
                label = "1ELIMK",
                description = "Elimäenkatu"
            )
        )
        val testable = HastusConverter.convertJoreTimingPlacesToHastus(joreTimingPlaces)

        val writer = CsvWriter()

        assertEquals(expectedResult, writer.transformToCsv(testable))
    }
}
