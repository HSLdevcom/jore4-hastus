package fi.hsl.jore4.hastus.test

import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

fun MutableMap<String, Any?>.getNested(propertyPath: String): MutableMap<String, Any?> {
    val parts = propertyPath.split(".")

    var child: MutableMap<String, Any?> = this
    for (childPath in parts) {
        LOGGER.debug { "Get path: $childPath" }

        if (childPath !in child) {
            throw IllegalStateException("No child item found at property '$childPath'")
        }

        @Suppress("UNCHECKED_CAST")
        child = child[childPath] as MutableMap<String, Any?>
    }

    return child
}
