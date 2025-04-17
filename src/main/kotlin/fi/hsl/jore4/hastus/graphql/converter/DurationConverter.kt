package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

class DurationConverter : ScalarConverter<Duration> {
    override fun toJson(value: Duration): String = value.toKotlinDuration().toIsoString()

    override fun toScalar(rawValue: Any): Duration =
        kotlin.time.Duration
            .parseIsoString(rawValue.toString())
            .toJavaDuration()
}
