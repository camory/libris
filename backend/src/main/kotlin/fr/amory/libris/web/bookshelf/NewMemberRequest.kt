package fr.amory.libris.web.bookshelf

import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.ReaderId
import java.util.UUID

data class NewMemberRequest(
  val readerId: UUID,
  val role: MembershipRole) {
  fun toMembership(): Membership =
    Membership(ReaderId(readerId), role)
}
