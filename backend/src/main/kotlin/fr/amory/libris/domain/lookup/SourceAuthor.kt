package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.AuthorRole

data class SourceAuthor(
    val name: String,
    val role: AuthorRole,
)
