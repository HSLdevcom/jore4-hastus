package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.test.TimetablesDatasetExtensions.PATTERN_ARRAY
import mu.KotlinLogging
import java.util.regex.Matcher
import java.util.regex.Pattern

private val LOGGER = KotlinLogging.logger {}

fun MutableMap<String, Any?>.getNested(propertyPath: String): MutableMap<String, Any?> {
    val tokens = propertyPath.split(".")

    var childProperties: MutableMap<String, Any?> = this
    for (token in tokens) {
        LOGGER.debug { "Getting property: $token" }

        val arrayMatcher: Matcher = PATTERN_ARRAY.matcher(token)

        val (propertyName: String, isArraySyntax: Boolean, arrayIndex: Int?) =
            if (arrayMatcher.matches()) {
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

                Triple(arrayPropName, true, index)
            } else {
                Triple(token, false, null)
            }

        val childElem: Any =
            childProperties[propertyName]
                ?: throw IllegalStateException("No descendant property was found by name '$propertyName'")

        when (childElem) {
            is List<*> -> {
                if (arrayIndex != null) {
                    @Suppress("UNCHECKED_CAST")
                    childProperties = childElem[arrayIndex] as MutableMap<String, Any?>
                } else {
                    childProperties = mutableMapOf()

                    childElem.forEachIndexed { index: Int, elem: Any? ->
                        childProperties[index.toString()] = elem
                    }
                }
            }

            // not a List
            else -> {
                if (isArraySyntax) {
                    throw IllegalStateException("Expected an array but did not get one: '$token'")
                }

                @Suppress("UNCHECKED_CAST")
                childProperties = childElem as MutableMap<String, Any?>
            }
        }
    }

    return childProperties
}

object TimetablesDatasetExtensions {
    private const val PATTERN_ARRAY_AS_STRING = "(.+)\\[(-?\\d*)]"

    internal val PATTERN_ARRAY: Pattern = Pattern.compile(PATTERN_ARRAY_AS_STRING)
}
