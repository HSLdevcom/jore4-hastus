package fi.hsl.jore4.hastus

import com.fasterxml.jackson.annotation.JsonInclude
import fi.hsl.jore4.hastus.config.HasuraConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

fun main(args: Array<String>) {
    runApplication<HastusApplication>(*args)
}

/**
 * Spring boot application definition.
 */
@SpringBootApplication
@EnableConfigurationProperties(HasuraConfiguration::class)
class HastusApplication {
    @Bean
    @Primary
    fun jsonMapper(): JsonMapper =
        JsonMapper
            .builder()
            .changeDefaultPropertyInclusion({ incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL) })
            .changeDefaultPropertyInclusion({ incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL) })
            .addModule(
                KotlinModule
                    .Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, false)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, false)
                    .configure(KotlinFeature.SingletonSupport, false)
                    .configure(KotlinFeature.StrictNullChecks, true)
                    .build()
            ).build()
}
