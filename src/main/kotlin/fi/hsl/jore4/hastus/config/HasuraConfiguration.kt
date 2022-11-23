package fi.hsl.jore4.hastus.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "hasura")
@ConstructorBinding
data class HasuraConfiguration(
    val url: String,
    val secret: String
)
