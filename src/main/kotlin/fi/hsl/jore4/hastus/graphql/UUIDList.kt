package fi.hsl.jore4.hastus.graphql

import java.util.UUID

// Container to get around type erasure
class UUIDList(
    val content: List<UUID>
)
