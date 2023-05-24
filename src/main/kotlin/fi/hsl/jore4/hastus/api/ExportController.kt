package fi.hsl.jore4.hastus.api

import com.fasterxml.jackson.annotation.JsonFormat
import fi.hsl.jore4.hastus.graphql.GraphQLService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
@RestController
@RequestMapping("export/")
class ExportController(
    private val graphQLService: GraphQLService
) {

    companion object {
        const val CSV_TYPE = "text/csv"

        private val LOGGER = KotlinLogging.logger {}
    }

    data class Routes(
        val uniqueLabels: List<String>,
        val priority: Int,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val observationDate: LocalDate
    )

    // Headers are not used by this service but passed on to the Hasura API
    @PostMapping("routes", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [CSV_TYPE])
    fun exportForRoutes(
        @RequestBody request: Routes,
        @RequestHeader headers: Map<String, String>
    ): String {
        val (result, elapsed) = measureTimedValue {
            LOGGER.debug { "Routes export request" }
            graphQLService.getRoutes(
                request.uniqueLabels,
                request.priority,
                request.observationDate,
                HasuraHeaders.filterHeaders(headers)
            )
        }
        LOGGER.info { "Routes request took $elapsed" }
        return result
    }

    @ExceptionHandler
    fun handleExportException(ex: Exception): ResponseEntity<String> {
        LOGGER.error { "Exception during request:$ex" }
        LOGGER.error(ex.stackTraceToString())
        return ResponseEntity("status", HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
