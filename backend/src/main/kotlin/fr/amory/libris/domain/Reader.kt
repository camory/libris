package fr.amory.libris.domain

import java.util.UUID

data class Reader(
    val id: UUID = newId(),
    val username: String,
    val email: String,
    val displayName: String,
    val defaultBookshelfId: UUID,
)
