package fi.hsl.jore4.hastus.api

object HeaderUtils {
    fun filterInHasuraHeaders(headers: Map<String, String>): Map<String, String> =
        headers.filter { it.key == "cookie" || it.key.startsWith("x-", true) }
}
