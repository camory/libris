package fr.amory.libris.library.domain.reader

data class Reader(
    val id: ReaderId,
    val username: String,
    val email: String,
    val displayName: String,
)
