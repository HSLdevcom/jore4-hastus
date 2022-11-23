package fi.hsl.jore4.hastus.graphql

// Container to get around type erasure
@JvmInline
value class IJSONB(
    val content: Map<String, String>
)
