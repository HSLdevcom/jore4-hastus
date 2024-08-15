package fi.hsl.jore4.hastus.util

object CollectionUtil {
    fun <T> filterOutConsecutiveDuplicates(iter: Iterable<T>): List<T> =
        (iter + null) // in order for the last item to be handled correctly, a dummy element must be added to the end
            .zipWithNext()
            .filter { (first, second) -> first != second }
            .mapNotNull { it.first }
}
