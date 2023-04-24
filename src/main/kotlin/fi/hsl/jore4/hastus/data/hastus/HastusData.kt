package fi.hsl.jore4.hastus.data.hastus

sealed class HastusData : IHastusData {

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
