package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.ReaderId
import java.time.Instant

data class Bookshelf(
  val id: BookshelfId,
  val name: BookshelfName,
  val memberships: List<Membership>,
  val createdBy: ReaderId,
  val createdAt: Instant) {
  init {
    require(memberships.any { it.role == OWNER }) { "a bookshelf needs an owner" }
    require(memberships.distinctBy { it.readerId } == memberships) { "a reader is a member of a bookshelf once" }
  }

  fun hasMember(readerId: ReaderId): Boolean =
    memberships.any { it.readerId == readerId }

  fun isOwnedBy(readerId: ReaderId): Boolean =
    memberships.any { it.readerId == readerId && it.role == OWNER }

  fun roleOf(readerId: ReaderId): MembershipRole? =
    memberships.firstOrNull { it.readerId == readerId }?.role

  fun withMember(membership: Membership): Bookshelf =
    copy(memberships = memberships + membership)

  companion object {
    fun created(id: BookshelfId, name: BookshelfName, by: ReaderId, at: Instant): Bookshelf =
      Bookshelf(id, name, listOf(Membership(by, OWNER)), by, at)

    fun ownedBy(owner: ReaderId, ownerName: String, id: BookshelfId, at: Instant): Bookshelf =
      created(id, BookshelfName("Bibliothèque de $ownerName"), owner, at)
  }
}
