package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_STOP_NAME_IN_FINNISH
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_STOP_NAME_IN_SWEDISH
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_FINNISH
import fi.hsl.jore4.hastus.Constants.MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_SWEDISH
import fi.hsl.jore4.hastus.data.format.Coordinate
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.hastus.Route
import fi.hsl.jore4.hastus.data.hastus.RouteVariant
import fi.hsl.jore4.hastus.data.hastus.Stop
import fi.hsl.jore4.hastus.data.jore.JoreLine
import fi.hsl.jore4.hastus.data.jore.JoreRoute
import fi.hsl.jore4.hastus.data.jore.JoreScheduledStop
import fi.hsl.jore4.hastus.service.exporting.ConversionsToHastus.getHastusRouteVariantId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

@DisplayName("Test type conversions from Jore to Hastus")
class ConversionsToHastusTest {

    @DisplayName("Test method: getHastusRouteVariantId")
    @Nested
    inner class TestGetHastusRouteVariantId {

        @Test
        fun `with basic route`() {
            assertEquals("1", getHastusRouteVariantId("123", "123", null, JoreRouteDirection.OUTBOUND))
            assertEquals("2", getHastusRouteVariantId("123", "123", null, JoreRouteDirection.INBOUND))
        }

        @Test
        fun `with one-char letter variant`() {
            assertEquals("B1", getHastusRouteVariantId("123", "123B", null, JoreRouteDirection.OUTBOUND))
            assertEquals("B2", getHastusRouteVariantId("123", "123B", null, JoreRouteDirection.INBOUND))
        }

        @Test
        fun `with two-char letter variant`() {
            assertEquals("BK1", getHastusRouteVariantId("123", "123BK", null, JoreRouteDirection.OUTBOUND))
            assertEquals("BK2", getHastusRouteVariantId("123", "123BK", null, JoreRouteDirection.INBOUND))
        }

        @Test
        fun `with single digit variant`() {
            assertEquals("31", getHastusRouteVariantId("123", "123", "3", JoreRouteDirection.OUTBOUND))
            assertEquals("32", getHastusRouteVariantId("123", "123", "3", JoreRouteDirection.INBOUND))
        }

        @Test
        fun `with variant consisting of one letter and a single digit`() {
            assertEquals("K41", getHastusRouteVariantId("123", "123K", "4", JoreRouteDirection.OUTBOUND))
            assertEquals("K42", getHastusRouteVariantId("123", "123K", "4", JoreRouteDirection.INBOUND))
        }

        @Test
        fun `with variant consisting of two letters and a single digit`() {
            assertEquals("BK51", getHastusRouteVariantId("123", "123BK", "5", JoreRouteDirection.OUTBOUND))
            assertEquals("BK52", getHastusRouteVariantId("123", "123BK", "5", JoreRouteDirection.INBOUND))
        }
    }

    @DisplayName("Test method: convertJoreLineToHastus")
    @Nested
    inner class TestConvertJoreLineToHastus {

        @Test
        fun `test truncation of length-limited fields`() {
            val name = "1234567890".repeat(MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION / 10 * 20)

            val hastusRoute: Route = ConversionsToHastus.convertJoreLineToHastus(
                JoreLine(
                    label = "123",
                    name = name,
                    typeOfLine = "stopping_bus_service",
                    vehicleMode = 0,
                    routes = emptyList()
                )
            )

            assertEquals(name.take(MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION), hastusRoute.description)
        }
    }

    @DisplayName("Test method: convertJoreRouteToHastusRouteVariant")
    @Nested
    inner class TestConvertJoreRouteToHastusRouteVariant {

        @Test
        fun `test truncation of length-limited fields`() {
            val name = "1234567890".repeat(MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION / 10 * 20)

            val hastusRouteVariant: RouteVariant = ConversionsToHastus.convertJoreRouteToHastusRouteVariant(
                JoreRoute(
                    label = "123B",
                    variant = null,
                    name = name,
                    direction = JoreRouteDirection.OUTBOUND,
                    reversible = false,
                    validityStart = LocalDate.of(2023, 1, 1),
                    validityEnd = LocalDate.of(2050, 12, 31),
                    typeOfLine = "stopping_bus_service",
                    journeyPatternId = UUID.randomUUID(),
                    stopPointsInJourneyPattern = emptyList()
                ),
                "123"
            )

            assertEquals(name.take(MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION), hastusRouteVariant.description)
        }
    }

    @DisplayName("Test method: convertJoreStopPointToHastus")
    @Nested
    inner class TestConvertJoreStopPointToHastus {

        @Test
        fun `test truncation of length-limited fields`() {
            val nameFi = "1234567890".repeat(MAX_LENGTH_HASTUS_STOP_NAME_IN_FINNISH / 10 * 20)
            val nameSv = "9876543210".repeat(MAX_LENGTH_HASTUS_STOP_NAME_IN_SWEDISH / 10 * 20)

            val streetNameFi = "ABCDEFGHIJ".repeat(MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_FINNISH / 10 * 20)
            val streetNameSv = "JIHGFEDCBA".repeat(MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_SWEDISH / 10 * 20)

            val hastusStop: Stop = ConversionsToHastus.convertJoreStopPointToHastus(
                JoreScheduledStop(
                    label = "H1234",
                    platform = "00",
                    nameFinnish = nameFi,
                    nameSwedish = nameSv,
                    streetNameFinnish = streetNameFi,
                    streetNameSwedish = streetNameSv,
                    timingPlaceShortName = "1AACKT",
                    location = Coordinate(24.928327, 60.163918)
                )
            )

            assertEquals(nameFi.take(MAX_LENGTH_HASTUS_STOP_NAME_IN_FINNISH), hastusStop.descriptionFinnish)
            assertEquals(nameSv.take(MAX_LENGTH_HASTUS_STOP_NAME_IN_SWEDISH), hastusStop.descriptionSwedish)

            assertEquals(streetNameFi.take(MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_FINNISH), hastusStop.streetFinnish)
            assertEquals(streetNameSv.take(MAX_LENGTH_HASTUS_STOP_STREET_NAME_IN_SWEDISH), hastusStop.streetSwedish)
        }
    }
}
