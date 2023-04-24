package fi.hsl.jore4.hastus.data.hastus

/**
 * Hastus data interface which represents a line of CSV data used to communicate with Hastus
 */
sealed interface IHastusData {

    /**
     * Get the data fields of the data, can be string or number
     *
     * @return
     */
    fun getFields(): List<Any>
}
