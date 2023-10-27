package fi.hsl.jore4.hastus.api

import com.fasterxml.jackson.databind.ObjectMapper
import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.service.importing.ImportService
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val LOGGER = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
@RestController
@RequestMapping("import")
class ImportController(
    private val importService: ImportService,
    private val objectMapper: ObjectMapper
) {

    data class ImportTimetablesSuccessResult(
        val vehicleScheduleFrameId: UUID?
    ) : Serializable

    data class ImportTimetablesFailureResult(
        val reason: String?
    ) : Serializable

    @PostMapping("", consumes = [MIME_TYPE_CSV], produces = [APPLICATION_JSON_VALUE])
    fun importCsvFile(
        @RequestBody csv: String,
        @RequestHeader headers: Map<String, String>
    ): ResponseEntity<String> {
        val (nullableVehicleScheduleFrameId, elapsed) = measureTimedValue {
            LOGGER.info("Starting to import timetables from CSV file...")

            val hasuraHeaders = HeaderUtils.filterInHasuraHeaders(headers)

            importService
                .importTimetablesFromCsv(csv, hasuraHeaders)
                .also { nullableUuid ->
                    if (nullableUuid == null) {
                        // Should never happen, but record log event if it actually does.
                        LOGGER.warn("CSV import did not result in vehicle schedule frame")
                    }
                }
        }

        LOGGER.info { "CSV import processing completed in $elapsed" }

        return ResponseEntity
            .ok()
            .body(
                serialise(
                    ImportTimetablesSuccessResult(nullableVehicleScheduleFrameId)
                )
            )
    }

    @ExceptionHandler
    fun handleExportException(ex: Exception): ResponseEntity<String> {
        return when (ex) {
            is ResponseStatusException -> {
                ResponseEntity
                    .status(ex.statusCode)
                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .body(
                        serialise(
                            ImportTimetablesFailureResult(ex.reason)
                        )
                    )
            }

            else -> {
                LOGGER.error { "Exception during import request:$ex" }
                LOGGER.error(ex.stackTraceToString())

                ResponseEntity
                    .internalServerError()
                    .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .body(
                        serialise(
                            ImportTimetablesFailureResult(ex.message)
                        )
                    )
            }
        }
    }

    private fun serialise(obj: Serializable): String = objectMapper.writeValueAsString(obj)
}
