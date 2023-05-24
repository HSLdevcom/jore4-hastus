package fi.hsl.jore4.hastus.graphql

import java.util.UUID

// Container to get around type erasure
// Not declared as value class because of support from Jackson
class UUIDList(
    val content: List<UUID>
)
