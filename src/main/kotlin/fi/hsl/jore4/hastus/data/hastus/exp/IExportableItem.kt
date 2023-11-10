package fi.hsl.jore4.hastus.data.hastus.exp

/**
 * Hastus data interface which represents a line of CSV data to be exported to Hastus.
 */
sealed interface IExportableItem {
    /**
     * Get the data fields of the data, can be string or number
     *
     * @return
     */
    fun getFields(): List<Any>
}
