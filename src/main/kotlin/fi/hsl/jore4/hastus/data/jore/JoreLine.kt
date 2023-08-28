package fi.hsl.jore4.hastus.data.jore

data class JoreLine(
    val label: String,
    val name: String,
    val typeOfLine: String,
    val vehicleMode: Int,
    val routes: List<JoreRoute>
)
