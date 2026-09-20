package fr.amory.libris.domain

import com.fasterxml.uuid.Generators
import fr.amory.libris.domain.MemberRole.OWNER
import java.util.UUID

data class Reader(
    val id: UUID = Generators.timeBasedEpochGenerator().generate(),
    val username: String,
    val email: String,
    val displayName: String,
    val memberships: List<Member>,
    val defaultBookshelfId: UUID,
) {
    constructor(username: String, email: String, displayName: String, bookshelf: Bookshelf) : this(
        username = username,
        email = email,
        displayName = displayName,
        memberships = listOf(Member(bookshelf.id, OWNER)),
        defaultBookshelfId = bookshelf.id,
    )

    init {
        require(memberships.any { it.bookshelfId == defaultBookshelfId }) {
            "the default bookshelf of $username is none of their bookshelves"
        }
    }
}
