package fi.hsl.jore4.hastus.test

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jore4.db")
data class TestDatabaseProperties(
    val url: String,
    val username: String,
    val password: String
)
