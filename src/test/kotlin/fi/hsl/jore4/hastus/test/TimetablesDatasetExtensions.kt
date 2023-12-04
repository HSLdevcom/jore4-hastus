package fi.hsl.jore4.hastus.test

import fi.hsl.jore4.hastus.test.TimetablesDatasetExtensions.PATTERN_INDEXED_ARRAY_ELEMENT
import fi.hsl.jore4.hastus.test.TimetablesDatasetExtensions.PATTERN_WHOLE_ARRAY
import mu.KotlinLogging
import java.util.regex.Matcher
import java.util.regex.Pattern

private val LOGGER = KotlinLogging.logger {}

fun MutableMap<String, Any?>.getNested(propertyPath: String): MutableMap<String, Any?> {
    val tokens = propertyPath.split(".")

    var childProperties: MutableMap<String, Any?> = this
    for (token in tokens) {
        LOGGER.debug { "Getting property: $token" }

        val wholeArrayMatcher: Matcher = PATTERN_WHOLE_ARRAY.matcher(token)
        val indexedArrayElemMatcher: Matcher = PATTERN_INDEXED_ARRAY_ELEMENT.matcher(token)

        val (propertyName: String, arrayIndex: Int?) =
            if (wholeArrayMatcher.matches()) {
                val arrayName = wholeArrayMatcher.group(1)

                arrayName to null
            } else if (indexedArrayElemMatcher.matches()) {
                val name = indexedArrayElemMatcher.group(1)
                val index = indexedArrayElemMatcher.group(2).toInt()

                if (index < 0) {
                    throw IllegalStateException("Index must not be negative: '$token'")
                }

                name to index
            } else {
                token to null
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
                @Suppress("UNCHECKED_CAST")
                childProperties = childElem as MutableMap<String, Any?>
            }
        }
    }

    return childProperties
}

object TimetablesDatasetExtensions {
    private const val PATTERN_WHOLE_ARRAY_AS_STRING = "(.+)\\[]"
    private const val PATTERN_INDEXED_ARRAY_ELEMENT_AS_STRING = "(.+)\\[(-?\\d+)]"

    internal val PATTERN_WHOLE_ARRAY: Pattern = Pattern.compile(PATTERN_WHOLE_ARRAY_AS_STRING)
    internal val PATTERN_INDEXED_ARRAY_ELEMENT: Pattern = Pattern.compile(PATTERN_INDEXED_ARRAY_ELEMENT_AS_STRING)
}
