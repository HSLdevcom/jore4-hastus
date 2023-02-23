package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.data.format.Coordinate
import fi.hsl.jore4.hastus.data.jore.JoreHastusPlace
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreRouteScheduledStop
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.data.mapper.HastusConverter
import fi.hsl.jore4.hastus.util.CsvWriter
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Test the Hastus converter")
class HastusConverterTest {

    @Test
    @DisplayName("When converting from lines")
    fun whenMappingLines() {
        // rvariant;1;Elielinaukio, l. 28-Kalajärvi;0;0;24361;2436
        val expectedResult = """
            route;65;Rautatientori - Veräjälaakso FI;0;0;0
            rvariant;1;Reitti A - B FI;1;0;651;65
            rvpoint;place;1.234;0;0;0;1AACKT;651
            rvpoint;place;1.000;0;0;1;1ELIMK;651
            rvpoint;place;2.500;0;0;0;1AURLA;651
            rvariant;2;Reitti A - B 3 FI;1;0;652;65
            rvpoint;place;1.234;0;0;0;1AACKT;652
            rvpoint;place;1.000;0;0;1;1ELIMK;652
            rvpoint;place;2.500;0;0;0;1AURLA;652
        """.trimIndent()

        val joreStops = listOf(
            JoreRouteScheduledStop(
                hastusPlace = "1AACKT",
                distance = 1234.0,
                isInUse = false,
                isAllowedLoad = false,
                isTimingPoint = false,
                stopLabel = "H1234"
            ),
            JoreRouteScheduledStop(
                hastusPlace = "1ELIMK",
                distance = 1000.0,
                isInUse = false,
                isAllowedLoad = false,
                isTimingPoint = true,
                stopLabel = "H1235"
            ),
            JoreRouteScheduledStop(
                hastusPlace = "1AURLA",
                distance = 2500.0,
                isInUse = false,
                isAllowedLoad = false,
                isTimingPoint = false,
                stopLabel = "H1236"
            )
        )
        val joreRoutes = listOf(
            JoreRoute("65x", "1", "651", "Reitti A - B FI", 1, false, joreStops),
            JoreRoute("65y", "2", "652", "Reitti A - B 3 FI", 1, false, joreStops)
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
                hastusPlace = "1AACKT",
                location = Coordinate(24.928327, 60.163918)
            ),
            JoreScheduledStop(
                label = "H1235",
                platform = "00",
                nameFinnish = "kuvaus2",
                nameSwedish = "beskrivning2",
                streetNameFinnish = "katu2",
                streetNameSwedish = "gata2",
                hastusPlace = "1ELIMK",
                location = Coordinate(24.930490, 60.164635)
            ),
            JoreScheduledStop(
                label = "H1236",
                platform = "00",
                nameFinnish = "kuvaus3",
                nameSwedish = "beskrivning3",
                streetNameFinnish = "katu3",
                streetNameSwedish = "gata3",
                hastusPlace = "1AURLA",
                location = Coordinate(24.933252, 60.165655)
            )
        )
        val testable = HastusConverter.convertJoreStopsToHastus(joreStops)

        val writer = CsvWriter()

        assertEquals(expectedResult, writer.transformToCsv(testable))
    }

    @Test
    @DisplayName("When converting Hastus places")
    fun whenMappingPlaces() {
        val expectedResult = """
            place;1AACKT;Aino Ackten tie
            place;1ELIMK;Elimäenkatu
        """.trimIndent()

        val jorePlaces = listOf(
            JoreHastusPlace(
                label = "1AACKT",
                description = "Aino Ackten tie"
            ),
            JoreHastusPlace(
                label = "1ELIMK",
                description = "Elimäenkatu"
            )
        )
        val testable = HastusConverter.convertJorePlacesToHastus(jorePlaces)

        val writer = CsvWriter()

        assertEquals(expectedResult, writer.transformToCsv(testable))
    }
}
