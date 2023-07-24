package fi.hsl.jore4.hastus.api

import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.util.CsvReader
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
    private val graphQLService: GraphQLService
) {

    companion object {
        const val CSV_TYPE = "text/csv"
    }

    @PostMapping("", consumes = [CSV_TYPE])
    fun importCsvFile(
        @RequestBody request: String,
        @RequestHeader headers: Map<String, String>
    ): String {
        val reader = CsvReader(";")

        val (result, elapsed) = measureTimedValue {
            LOGGER.debug { "CSV import request" }
            val hastusItems: List<IHastusData> = reader.parseCsv(request)
            val filteredHeaders = HeaderUtils.filterInHasuraHeaders(headers)

            val hastusRoutes: List<String> = hastusItems.filterIsInstance<TripRecord>().map { it.tripRoute }
            val hastusBookingRecordName: String = hastusItems.filterIsInstance<BookingRecord>().first().name

            graphQLService.persistTimetable(filteredHeaders, hastusRoutes, hastusBookingRecordName, hastusItems)
            "200"
        }

        LOGGER.info { "CSV import request took $elapsed" }

        return result
    }
}
