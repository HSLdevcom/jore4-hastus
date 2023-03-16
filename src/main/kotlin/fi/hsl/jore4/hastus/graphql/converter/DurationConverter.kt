package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.converter.ScalarConverter
import kotlin.time.Duration

class DurationConverter : ScalarConverter<Duration> {
    override fun toJson(value: Duration): Any {
        return value.toIsoString()
    }

    override fun toScalar(rawValue: Any): Duration {
        return Duration.parseIsoString(rawValue.toString())
    }
}
