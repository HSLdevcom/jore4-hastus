package fi.hsl.jore4.hastus.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionUtilTest {

    @Nested
    @DisplayName("Method: filterOutConsecutiveDuplicates")
    inner class FilterOutConsecutiveDuplicates {

        @Test
        fun `test empty`() {
            assertEquals(
                listOf<Int>(),
                CollectionUtil.filterOutConsecutiveDuplicates(listOf<Int>())
            )
        }

        @Test
        fun `test without duplicates`() {
            assertEquals(
                listOf(1, 2, 3),
                CollectionUtil.filterOutConsecutiveDuplicates(listOf(1, 2, 3))
            )
        }

        @Test
        fun `test with duplicates at start`() {
            assertEquals(
                listOf(1, 2, 3),
                CollectionUtil.filterOutConsecutiveDuplicates(listOf(1, 1, 2, 3))
            )
        }

        @Test
        fun `test with duplicates in the middle`() {
            assertEquals(
                listOf(1, 2, 3),
                CollectionUtil.filterOutConsecutiveDuplicates(listOf(1, 2, 2, 3))
            )
        }

        @Test
        fun `test with duplicates at end`() {
            assertEquals(
                listOf(1, 2, 3),
                CollectionUtil.filterOutConsecutiveDuplicates(listOf(1, 2, 3, 3))
            )
        }

        @Test
        fun `test with many duplicates`() {
            assertEquals(
                listOf(1, 2, 3, 4, 5, 6),
                CollectionUtil.filterOutConsecutiveDuplicates(listOf(1, 1, 1, 2, 3, 3, 4, 5, 5, 5, 6))
            )
        }
    }
}
