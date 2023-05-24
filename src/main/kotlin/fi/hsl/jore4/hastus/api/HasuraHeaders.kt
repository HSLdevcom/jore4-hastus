package fi.hsl.jore4.hastus.api

class HasuraHeaders {
    companion object {
        fun filterHeaders(headers: Map<String, String>): Map<String, String> {
            return headers.filter { it.key == "cookie" || it.key.startsWith("x-", true) }
        }
    }
}
