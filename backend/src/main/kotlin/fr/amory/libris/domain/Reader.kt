package fr.amory.libris.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

data class Reader(
    val id: UUID = Generators.timeBasedEpochGenerator().generate(),
    val username: String,
    val email: String,
    val displayName: String,
    val memberships: List<Member>,
    val defaultBookshelfId: UUID,
) {
    init {
        require(memberships.any { it.bookshelfId == defaultBookshelfId }) {
            "the default bookshelf of $username is none of their bookshelves"
        }
    }
}
