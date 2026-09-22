package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.ReaderId

data class Bookshelf(
    val id: BookshelfId,
    val name: String,
    val memberships: List<Membership>,
) {
    init {
        require(memberships.any { it.role == OWNER }) { "a bookshelf needs an owner" }
    }

    fun hasMember(readerId: ReaderId): Boolean = memberships.any { it.readerId == readerId }

    fun isOwnedBy(readerId: ReaderId): Boolean = memberships.any { it.readerId == readerId && it.role == OWNER }

    companion object {
        fun ownedBy(owner: ReaderId, ownerName: String, id: BookshelfId): Bookshelf =
            Bookshelf(id, "Bibliothèque de $ownerName", listOf(Membership(owner, OWNER)))
    }
}
