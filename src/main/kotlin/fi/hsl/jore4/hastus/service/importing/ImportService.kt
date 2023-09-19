package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.data.hastus.IHastusData
import fi.hsl.jore4.hastus.data.hastus.TripRecord
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPattern
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.data.mapper.ConversionsFromHastus
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
        val graphQLService: GraphQLService = graphQLServiceFactory.createForSession(hasuraHeaders)

        val hastusItems: List<IHastusData> = READER.parseCsv(csv)
        val hastusRoutes: List<String> = hastusItems.filterIsInstance<TripRecord>().map { it.tripRoute }

        val journeyPatternsIndexedByRouteLabel: Map<String, JoreJourneyPattern> =
            graphQLService.getJourneyPatternsIndexingByRouteLabel(hastusRoutes)
        LOGGER.debug { "Importing got journey patterns $journeyPatternsIndexedByRouteLabel" }

        val vehicleTypeIndex: Map<Int, UUID> = graphQLService.getVehicleTypes()
        LOGGER.debug { "Importing got vehicle types $vehicleTypeIndex" }

        val dayTypeIndex: Map<String, UUID> = graphQLService.getDayTypes()
        LOGGER.debug { "Importing got day types $dayTypeIndex" }

        val vehicleScheduleFrame: JoreVehicleScheduleFrame = ConversionsFromHastus.convertHastusDataToJore(
            hastusItems,
            journeyPatternsIndexedByRouteLabel,
            vehicleTypeIndex,
            dayTypeIndex
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
