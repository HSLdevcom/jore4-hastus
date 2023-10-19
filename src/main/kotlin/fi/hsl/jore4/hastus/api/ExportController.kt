package fi.hsl.jore4.hastus.api

import com.fasterxml.jackson.annotation.JsonFormat
import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.service.exporting.ExportService
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
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val LOGGER = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
@RestController
@RequestMapping("export/")
class ExportController(
    private val exportService: ExportService
) {

    data class ExportRoutesRequest(
        val uniqueLabels: List<String>,
        val priority: Int,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val observationDate: LocalDate
    )

    // Headers are not used by this service but passed on to the Hasura API
    @PostMapping("routes", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MIME_TYPE_CSV])
    fun exportForRoutes(
        @RequestBody request: ExportRoutesRequest,
        @RequestHeader headers: Map<String, String>
    ): ResponseEntity<String> {
        val (result, elapsed) = measureTimedValue {
            LOGGER.debug { "Routes export request" }

            exportService.exportRoutes(
                request.uniqueLabels,
                request.priority,
                request.observationDate,
                HeaderUtils.filterInHasuraHeaders(headers)
            )
        }

        LOGGER.info { "Routes request took $elapsed" }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result)
    }

    @ExceptionHandler
    fun handleExportException(ex: Exception): ResponseEntity<String> {
        return when (ex) {
            is ResponseStatusException -> ResponseEntity.status(ex.statusCode).body(ex.reason)

            else -> {
                LOGGER.error { "Exception during request:$ex" }
                LOGGER.error(ex.stackTraceToString())

                ResponseEntity("status", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
