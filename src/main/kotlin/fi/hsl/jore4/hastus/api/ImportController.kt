package fi.hsl.jore4.hastus.api

import fi.hsl.jore4.hastus.Constants.MIME_TYPE_CSV
import fi.hsl.jore4.hastus.api.util.HastusApiErrorType
import fi.hsl.jore4.hastus.service.importing.ImportService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val LOGGER = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
@RestController
@RequestMapping("import")
class ImportController(
    private val importService: ImportService
) {
    data class ImportTimetablesSuccessResult(
        val vehicleScheduleFrameId: UUID?
    )

    data class ImportTimetablesFailureResult(
        val type: HastusApiErrorType,
        val reason: String?
    )

    @PostMapping("", consumes = [MIME_TYPE_CSV])
    fun importCsvFile(
        @RequestBody csv: String,
        @RequestHeader headers: Map<String, String>
    ): ImportTimetablesSuccessResult {
        val (nullableVehicleScheduleFrameId, elapsed) =
            measureTimedValue {
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

        return ImportTimetablesSuccessResult(nullableVehicleScheduleFrameId)
    }

    @ExceptionHandler
    fun handleExportException(ex: Exception): ResponseEntity<ImportTimetablesFailureResult> =
        when (ex) {
            is ResponseStatusException -> {
                ResponseEntity
                    .status(ex.statusCode)
                    .body(ImportTimetablesFailureResult(HastusApiErrorType.from(ex), ex.reason))
            }

            else -> {
                LOGGER.error { "Exception during import request:$ex" }
                LOGGER.error(ex.stackTraceToString())

                ResponseEntity
                    .internalServerError()
                    .body(ImportTimetablesFailureResult(HastusApiErrorType.from(ex), ex.message))
            }
        }
}
