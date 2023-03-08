package fi.hsl.jore4.hastus.data.hastus

abstract class HastusData : IHastusData {

    /**
     * @value fieldName identifier of the element in Hastus readable csv
     */
    abstract val fieldName: String

    /**
     * Appends a given list with the csv identifier of the
     *
     * @param strings List of elements to append after the row identifier
     * @return [strings] list prepended with the row identifier
     */
    protected fun listWithFieldName(vararg strings: Any): List<Any> {
        return listOf(fieldName, *strings)
    }

    companion object {
        @JvmStatic
        protected fun parseToDouble(string: String): Double {
            return string.trim().toDoubleOrNull() ?: 0.0
        }

        @JvmStatic
        protected fun parseToInt(string: String): Int {
            return string.trim().toIntOrNull() ?: 0
        }

        @JvmStatic
        protected fun parseToBoolean(string: String): Boolean {
            return parseToInt(string) > 0
        }
    }
}
