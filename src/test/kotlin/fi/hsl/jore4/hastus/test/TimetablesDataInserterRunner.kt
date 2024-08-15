package fi.hsl.jore4.hastus.test

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.io.File
import java.io.Reader
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@Component
@EnableConfigurationProperties(TestDatabaseProperties::class)
class TimetablesDataInserterRunner(
    val databaseProperties: TestDatabaseProperties
) {
    fun truncateAndInsertDataset(datasetJson: String): String {
        val inserterWorkDirectory = Paths.get("jore4-hasura/test/hasura").toAbsolutePath().toString()

        // Note: this performs DB truncation internally.
        val command = "yarn --silent timetables-data-inserter:cli insert hsl ${buildDatabaseArguments()}"

        val process =
            ProcessBuilder(*command.split(" ").toTypedArray())
                .directory(File(inserterWorkDirectory))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        process.outputStream.use {
            val writer = it.bufferedWriter()
            writer.write(datasetJson)
            writer.flush()
        }

        val exitedBeforeTimeout = process.waitFor(10, TimeUnit.SECONDS)
        if (!exitedBeforeTimeout) {
            throw RuntimeException("Running timetables-data-inserter timed out.")
        }

        return process.inputStream.bufferedReader().use(Reader::readText)
    }

    private fun buildDatabaseArguments(): String = databaseProperties.run { buildDatabaseArguments(url, username, password) }

    companion object {
        private fun buildDatabaseArguments(
            jdbcUrl: String,
            username: String,
            password: String
        ): String {
            val cleanJdbcUri: String = jdbcUrl.substringAfter("jdbc:") // Strip leading "jdbc:"

            return URI.create(cleanJdbcUri)
                .let { uri ->
                    val databaseName: String = uri.path.substringAfter("/") // Strip leading "/"

                    listOf(
                        "--host ${uri.host}",
                        "--port ${uri.port}",
                        "--database $databaseName",
                        "--user $username",
                        "--password $password"
                    ).joinToString(" ")
                }
        }
    }
}
