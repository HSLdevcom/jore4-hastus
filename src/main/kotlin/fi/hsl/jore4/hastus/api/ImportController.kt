package fi.hsl.jore4.hastus.api

import fi.hsl.jore4.hastus.service.importing.ImportService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val LOGGER = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
@RestController
@RequestMapping("import")
class ImportController(
    private val importService: ImportService
) {

    companion object {
        const val CSV_TYPE = "text/csv"
    }

    @PostMapping("", consumes = [CSV_TYPE])
    fun importCsvFile(
        @RequestBody csv: String,
        @RequestHeader headers: Map<String, String>
    ): String {
        val (result, elapsed) = measureTimedValue {
            LOGGER.debug { "CSV import request" }

            val hasuraHeaders = HeaderUtils.filterInHasuraHeaders(headers)

            importService.importTimetablesFromCsv(csv, hasuraHeaders)

            "200"
        }

        LOGGER.info { "CSV import request took $elapsed" }

        return result
    }
}
