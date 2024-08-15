package fi.hsl.jore4.hastus.util

import fi.hsl.jore4.hastus.Constants
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object DateTimeUtil {
    fun currentDateTimeAtDefaultZone(): OffsetDateTime = OffsetDateTime.now(getDefaultZoneId())

    /**
     * Converts [LocalDate] to a [OffsetDateTime] at start of day using application's default time
     * zone.
     */
    fun LocalDate.toOffsetDateTimeAtDefaultZone(): OffsetDateTime {
        return ZonedDateTime
            .of(atStartOfDay(), getDefaultZoneId())
            .toOffsetDateTime()
    }

    fun getDefaultZoneId(): ZoneId = ZoneId.of(Constants.DEFAULT_TIMEZONE)
}
