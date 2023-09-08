package fi.hsl.jore4.hastus.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "hasura")
data class HasuraConfiguration(
    val url: String,
    val secret: String
)
