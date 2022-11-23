package fi.hsl.jore4.hastus.data

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
}
