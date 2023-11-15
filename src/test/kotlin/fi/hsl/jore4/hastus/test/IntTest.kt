package fi.hsl.jore4.hastus.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * A custom annotation which configures integration tests which use the test database settings.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@SpringBootTest
@ActiveProfiles("test")
annotation class IntTest
