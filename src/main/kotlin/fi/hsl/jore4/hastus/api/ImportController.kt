package fi.hsl.jore4.hastus.api

import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.mapper.JoreConverter
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.util.CsvReader
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
@RestController
@RequestMapping("import")
class ImportController(
    private val graphQLService: GraphQLService
) {

    companion object {
        const val CSV_TYPE = "text/csv"
    }

    private val LOGGER = KotlinLogging.logger {}

    @PostMapping("", consumes = [CSV_TYPE])
    fun importCsvFile(
        @RequestBody request: String,
        @RequestHeader(HttpHeaders.COOKIE, required = false) cookieHeader: String?,
        @RequestHeader("x-hasura-role", required = false) hasuraRole: String?,
        @RequestHeader("x-hasura-admin-secret", required = false) hasuraSecret: String?
    ): String {
        val (result, elapsed) = measureTimedValue {
            LOGGER.debug { "CSV import request" }
            val reader = CsvReader(";")
            val parsedValues = reader.parseCsv(request)

            val routesOfPatterns = parsedValues.filterIsInstance<TripRecord>().map { it.tripRoute }
            val name = parsedValues.filterIsInstance<BookingRecord>().first().name

            val journeyPatterns = graphQLService.getJourneyPatternsForRoutes(routesOfPatterns, cookieHeader, hasuraRole, hasuraSecret)
            LOGGER.trace { "Importing got journey patterns $journeyPatterns" }

            val vehicleTypes = graphQLService.getVehicleTypes(cookieHeader, hasuraRole, hasuraSecret)
            LOGGER.trace { "Importing got vehicle types $vehicleTypes" }

            val dayTypes = graphQLService.getDayTypes(cookieHeader, hasuraRole, hasuraSecret)
            LOGGER.trace { "Importing got day types $dayTypes" }

            val vehicleScheduleFrame = JoreConverter.convertHastusDataToJore(name, parsedValues, journeyPatterns, vehicleTypes, dayTypes)
            graphQLService.persistVehicleScheduleFrame(
                journeyPatterns.values,
                vehicleScheduleFrame,
                cookieHeader,
                hasuraRole,
                hasuraSecret
            )
            "200"
        }
        LOGGER.info { "CSV import request took $elapsed" }
        return result
    }
}