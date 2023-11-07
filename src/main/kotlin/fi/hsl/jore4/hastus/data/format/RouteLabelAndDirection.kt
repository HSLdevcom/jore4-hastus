package fi.hsl.jore4.hastus.data.format

data class RouteLabelAndDirection(
    val routeLabel: String,
    val direction: JoreRouteDirection
) : Comparable<RouteLabelAndDirection> {

    // Sorting is used e.g. for lists that are included in log or exception messages.
    override fun compareTo(other: RouteLabelAndDirection): Int {
        val labelComparison: Int = routeLabel.compareTo(other.routeLabel)

        // Sort outbound first and inbound after that.
        // The rest are set mutually equal and greater than the two aforementioned in terms of
        // sort order (because in-/outbound directions are only used in Hastus).
        fun getDirectionAsInt(dir: JoreRouteDirection): Int = dir.wellKnownNumber ?: 999

        return when (labelComparison) {
            0 -> getDirectionAsInt(direction).compareTo(getDirectionAsInt(other.direction))
            else -> labelComparison
        }
    }

    override fun toString() = "$routeLabel (${direction.toString().lowercase()})"
}
