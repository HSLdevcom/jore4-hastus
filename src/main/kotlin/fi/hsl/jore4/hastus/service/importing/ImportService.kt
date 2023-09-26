package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.hastus.BookingRecord
import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.graphql.GraphQLService
import fi.hsl.jore4.hastus.graphql.GraphQLServiceFactory
import fi.hsl.jore4.hastus.util.CsvReader
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

private val LOGGER = KotlinLogging.logger {}

@Service
class ImportService(private val graphQLServiceFactory: GraphQLServiceFactory) {

    fun importTimetablesFromCsv(
        csv: String,
        hasuraHeaders: Map<String, String>
    ): UUID? {
        val hastusItems: List<IHastusData> = READER.parseCsv(csv)
        val graphQLService: GraphQLService = graphQLServiceFactory.createForSession(hasuraHeaders)

        val hastusBookingRecord: BookingRecord = hastusItems.filterIsInstance<BookingRecord>().first()

        val hastusTrips: List<TripRecord> = hastusItems.filterIsInstance<TripRecord>()
        val uniqueRouteLabels: List<String> = hastusTrips
            .map(ConversionsFromHastus::extractRouteLabel)
            .distinct() // TODO: is distinct operation really required?

        val journeyPatternsIndexedByRouteLabel: Map<String, JoreJourneyPattern> =
            graphQLService.getJourneyPatternsIndexingByRouteLabel(
                uniqueRouteLabels,
                hastusBookingRecord.startDate,
                hastusBookingRecord.endDate
            )
        LOGGER.debug { "Importing got journey patterns $journeyPatternsIndexedByRouteLabel" }

        val vehicleTypeIndex: Map<Int, UUID> = graphQLService.getVehicleTypes()
        LOGGER.debug { "Importing got vehicle types $vehicleTypeIndex" }

        val dayTypeIndex: Map<String, UUID> = graphQLService.getDayTypes()
        LOGGER.debug { "Importing got day types $dayTypeIndex" }

        val vehicleScheduleFrame: JoreVehicleScheduleFrame = ConversionsFromHastus.convertHastusDataToJore(
            hastusItems,
            vehicleTypeIndex,
            dayTypeIndex,
            journeyPatternsIndexedByRouteLabel
        )

        return graphQLService.persistVehicleScheduleFrame(
            journeyPatternsIndexedByRouteLabel.values,
            vehicleScheduleFrame
        )
    }

    companion object {
        private val READER = CsvReader(";")
    }
}
