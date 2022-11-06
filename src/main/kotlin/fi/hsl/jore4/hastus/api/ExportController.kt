package fi.hsl.jore4.hastus.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ExportController {
    @GetMapping
    fun helloWorld(): Map<String, String> {
        return mapOf<String, String>("content" to "Hello World")
    }
}
