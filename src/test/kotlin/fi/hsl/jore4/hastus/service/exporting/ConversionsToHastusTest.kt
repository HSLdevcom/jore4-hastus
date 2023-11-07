package fi.hsl.jore4.hastus.service.exporting

import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.service.exporting.ConversionsToHastus.getHastusRouteVariantId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
        fun `with letter-number variant`() {
            assertEquals("K41", getHastusRouteVariantId("123", "123K", "4", JoreRouteDirection.OUTBOUND))
            assertEquals("K42", getHastusRouteVariantId("123", "123K", "4", JoreRouteDirection.INBOUND))
        }
    }
}
