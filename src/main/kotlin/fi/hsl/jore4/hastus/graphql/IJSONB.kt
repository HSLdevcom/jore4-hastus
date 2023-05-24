package fi.hsl.jore4.hastus.graphql

// Container to get around type erasure
// Not declared as value class because of support from Jackson
class IJSONB(
    val content: Map<String, String>
)
