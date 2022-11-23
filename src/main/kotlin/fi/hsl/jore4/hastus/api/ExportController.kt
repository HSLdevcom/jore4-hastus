package fi.hsl.jore4.hastus.api

import com.fasterxml.jackson.annotation.JsonFormat
import fi.hsl.jore4.hastus.graphql.GraphQLService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("export/")
class ExportController(
    private val graphQLService: GraphQLService
) {

    companion object {
        const val CSV_TYPE = "text/csv"
    }
    data class Lines(
        val labels: List<String>,
        val priority: Int,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val observationDate: LocalDate
    )

    data class Routes(
        val labels: List<String>,
        val priority: Int,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val observationDate: LocalDate
    )

    @PostMapping("lines", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [CSV_TYPE])
    fun exportForLines(
        @RequestBody request: Lines,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authHeader: String?,
        @RequestHeader("x-hasura-role", required = false) hasuraRole: String?,
        @RequestHeader("x-hasura-admin-secret", required = false) hasuraSecret: String?
    ): String {
        println(hasuraRole)
        return graphQLService.getRoutesForLines(
            request.labels,
            request.priority,
            request.observationDate,
            authHeader,
            hasuraRole,
            hasuraSecret
        )
    }

    @PostMapping("routes", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [CSV_TYPE])
    fun exportForRoutes(
        @RequestBody request: Routes,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authHeader: String?,
        @RequestHeader("x-hasura-role", required = false) hasuraRole: String?,
        @RequestHeader("x-hasura-admin-secret", required = false) hasuraSecret: String?
    ): String {
        println(hasuraRole)
        return graphQLService.getRoutesForRoutes(
            request.labels,
            request.priority,
            request.observationDate,
            authHeader,
            hasuraRole,
            hasuraSecret
        )
    }
}
