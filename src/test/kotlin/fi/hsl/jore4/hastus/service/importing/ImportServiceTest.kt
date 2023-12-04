package fi.hsl.jore4.hastus.service.importing

import fi.hsl.jore4.hastus.TestConstants.HASURA_ROLE
import fi.hsl.jore4.hastus.data.format.JoreRouteDirection
import fi.hsl.jore4.hastus.data.format.RouteLabelAndDirection
import fi.hsl.jore4.hastus.test.IntTest
import fi.hsl.jore4.hastus.test.TimetablesDataInserterRunner
import fi.hsl.jore4.hastus.test.TimetablesDataset
import fi.hsl.jore4.hastus.test.getNested
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.util.FileCopyUtils
import java.io.IOException
import java.io.InputStreamReader
import java.io.UncheckedIOException
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

private val LOGGER = KotlinLogging.logger {}

@IntTest
class ImportServiceTest(
    @Autowired private val service: ImportService,
    @Autowired private val dataInserterRunner: TimetablesDataInserterRunner,
    @Value("\${hasura.secret}") private val hasuraSecret: String
) {
    private fun populateDatabaseFromDataset(dataset: TimetablesDataset): String =
        dataInserterRunner
            .truncateAndInsertDataset(dataset.toJSONString())
            .also { json ->
                // Change log level to e.g. DEBUG see what is being persisted into database.
                LOGGER.trace { "Persisting dataset:\n$json" }
            }

    private fun populateDatabaseFromDataset(pathToFile: String): String {
        val dataset = TimetablesDataset.createFromResource(pathToFile)

        return populateDatabaseFromDataset(dataset)
    }

    private fun getHasuraHeaders(): Map<String, String> =
        mapOf(
            "X-Hasura-Role" to HASURA_ROLE,
            "X-Hasura-Admin-Secret" to hasuraSecret
        )

    @Test
    fun `when journey-pattern-ref exists for both directions`() {
        populateDatabaseFromDataset("datasets/journey_pattern_ref_100_starting_2023.json")

        val csvToImport: String = readFileAsString("hastus_booking_records/100_both_directions.exp")

        val vsfId: UUID? = service.importTimetablesFromCsv(csvToImport, getHasuraHeaders())

        assertNotNull(vsfId, "Vehicle schedule frame ID must not be null")
    }

    @Test
    fun `when the start date of the booking record is earlier than the route of the related journey pattern`() {
        val dataset = TimetablesDataset.createFromResource("datasets/journey_pattern_ref_100_starting_2023.json")

        // Mutate the dataset by setting the route start from 2024-01-01.
        dataset.getNested("_journey_pattern_refs.route100Outbound")["route_validity_start"] = "2024-01-01"

        populateDatabaseFromDataset(dataset)

        val csvToImport: String = readFileAsString("hastus_booking_records/100_both_directions.exp")

        assertFailsWith<CannotFindJourneyPatternRefByRouteLabelAndDirectionException> {
            service.importTimetablesFromCsv(csvToImport, getHasuraHeaders())
        }.also { exception ->
            // Fails for the first journey-pattern/route encountered that happens to be the outbound
            // direction for the line "100" (as this is the first Hastus trip in the file).
            assertEquals(
                exception.reason,
                "Could not find journey pattern reference for Hastus trips with the following route labels and directions: 100 (outbound)"
            )
        }
    }

    @Test
    fun `when journey-pattern-ref not found for one direction`() {
        val dataset = TimetablesDataset.createFromResource("datasets/journey_pattern_ref_100_starting_2023.json")

        // Mutate the dataset by removing journey pattern reference for the inbound direction.
        dataset.getNested("_journey_pattern_refs").remove("route100Inbound")

        populateDatabaseFromDataset(dataset)

        val csvToImport: String = readFileAsString("hastus_booking_records/100_both_directions.exp")

        assertFailsWith<CannotFindJourneyPatternRefByRouteLabelAndDirectionException> {
            service.importTimetablesFromCsv(csvToImport, getHasuraHeaders())
        }.also { exception ->
            assertEquals(
                exception.reason,
                "Could not find journey pattern reference for Hastus trips with the following route labels and directions: 100 (inbound)"
            )
        }
    }

    @Test
    fun `when the stop point labels do not match between Hastus trip and journey-pattern-ref`() {
        val dataset = TimetablesDataset.createFromResource("datasets/journey_pattern_ref_100_starting_2023.json")

        // Mutate the dataset by only changing the label for the third stop point.
        dataset.getNested("_journey_pattern_refs.route100Outbound._stop_points[2]")["scheduled_stop_point_label"] =
            "X1021"

        populateDatabaseFromDataset(dataset)

        val csvToImport: String = readFileAsString("hastus_booking_records/100_both_directions.exp")

        assertFailsWith<CannotFindJourneyPatternRefByStopPointLabelsException> {
            service.importTimetablesFromCsv(csvToImport, getHasuraHeaders())
        }.also { exception ->
            assertEquals(exception.routeIdentifier, RouteLabelAndDirection("100", JoreRouteDirection.OUTBOUND))
            assertEquals(exception.stopLabels, listOf("H1001", "H1011", "H1021", "H1031"))
        }
    }

    @Test
    fun `when the timing place labels do not match between Hastus trip and journey-pattern-ref`() {
        val dataset = TimetablesDataset.createFromResource("datasets/journey_pattern_ref_100_starting_2023.json")

        // Mutate the dataset by only changing the timing place label for the last (4th) stop point.
        dataset.getNested("_journey_pattern_refs.route100Outbound._stop_points[3]")["timing_place_label"] =
            "TP999"

        populateDatabaseFromDataset(dataset)

        val csvToImport: String = readFileAsString("hastus_booking_records/100_both_directions.exp")

        assertFailsWith<CannotFindJourneyPatternRefByTimingPlaceLabelsException> {
            service.importTimetablesFromCsv(csvToImport, getHasuraHeaders())
        }.also { exception ->
            assertEquals(exception.routeIdentifier, RouteLabelAndDirection("100", JoreRouteDirection.OUTBOUND))
            assertEquals(exception.stopLabels, listOf("H1001", "H1011", "H1021", "H1031"))
            assertEquals(exception.placeCodes, listOf("TP001", null, null, "TP002"))
        }
    }

    companion object {
        private fun readAsString(resource: Resource): String {
            try {
                return InputStreamReader(resource.inputStream, Charsets.ISO_8859_1)
                    .use { reader -> FileCopyUtils.copyToString(reader) }
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
        }

        private fun readFileAsString(pathToFile: String): String {
            val resourceLoader: ResourceLoader = DefaultResourceLoader()
            val resource: Resource = resourceLoader.getResource(pathToFile)

            return readAsString(resource)
                .also { contents ->
                    LOGGER.debug { "Reading file from path: $pathToFile\n$contents" }
                }
        }
    }
}
