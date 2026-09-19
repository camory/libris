package fr.amory.libris.domain

import java.util.UUID

data class Bookshelf(
    val id: UUID = newId(),
    val name: String,
    val members: List<Member>,
)
