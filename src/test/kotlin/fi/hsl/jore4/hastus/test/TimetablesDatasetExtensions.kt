package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.test.TimetablesDatasetExtensions.extractChildProperties
import mu.KotlinLogging
import java.util.regex.Pattern

private val LOGGER = KotlinLogging.logger {}

fun MutableMap<String, Any?>.getNested(propertyPath: String): MutableMap<String, Any?> =
    propertyPath
        .split(".")
        .fold(this) { properties: MutableMap<String, Any?>, propertyRef: String ->
            LOGGER.debug { "Extracting child properties: $propertyRef" }

            extractChildProperties(properties, propertyRef)
        }

object TimetablesDatasetExtensions {
    private val PATTERN_ARRAY: Pattern = Pattern.compile("(.+)\\[(-?\\d*)]")

    internal fun extractChildProperties(
        properties: MutableMap<String, Any?>,
        propertyRef: String
    ): MutableMap<String, Any?> {
        val (propertyName: String, isArraySyntax: Boolean, arrayIndex: Int?) =
            getArrayPropertyNameAndIndexOrNull(propertyRef)
                ?.let { (arrayPropName, index) ->
                    Triple(arrayPropName, true, index)
                }
                ?: Triple(propertyRef, false, null)

        val childElem: Any =
            properties[propertyName]
                ?: throw IllegalStateException("No descendant property was found by: '$propertyName'")

        return when (childElem) {
            is List<*> -> {
                if (arrayIndex != null) {
                    @Suppress("UNCHECKED_CAST")
                    childElem[arrayIndex] as MutableMap<String, Any?>
                } else {
                    childElem
                        .mapIndexed { index: Int, elem: Any? ->
                            index.toString() to elem
                        }
                        .toMap()
                        .toMutableMap()
                }
            }

            // not a List
            else -> {
                if (isArraySyntax) {
                    throw IllegalStateException("Expected an array but did not get one: '$propertyRef'")
                }

                @Suppress("UNCHECKED_CAST")
                childElem as MutableMap<String, Any?>
            }
        }
    }

    private fun getArrayPropertyNameAndIndexOrNull(token: String): Pair<String, Int?>? =
        PATTERN_ARRAY
            .matcher(token)
            .takeIf { it.matches() }
            ?.let { arrayMatcher ->
                val arrayPropName: String = arrayMatcher.group(1)

                val index: Int? =
                    arrayMatcher
                        .group(2)
                        .takeUnless { it.isEmpty() }
                        ?.toInt()
                        ?.also {
                            if (it < 0) {
                                throw IllegalStateException("Index must not be negative: '$token'")
                            }
                        }

                arrayPropName to index
            }
}
