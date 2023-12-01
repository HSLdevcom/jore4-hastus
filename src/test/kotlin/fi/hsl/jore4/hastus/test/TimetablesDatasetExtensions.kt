package fi.hsl.jore4.hastus.test

import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

fun MutableMap<String, Any?>.getNested(propertyPath: String): MutableMap<String, Any?> {
    val tokens = propertyPath.split(".")

    var childProperties: MutableMap<String, Any?> = this
    for (propertyName in tokens) {
        LOGGER.debug { "Getting property: $propertyName" }

        if (propertyName !in childProperties) {
            throw IllegalStateException("No descendant property was found by name '$propertyName'")
        }

        @Suppress("UNCHECKED_CAST")
        childProperties = childProperties[propertyName] as MutableMap<String, Any?>
    }

    return childProperties
}
