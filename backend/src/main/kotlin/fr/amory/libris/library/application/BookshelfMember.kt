package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.ReaderId

data class BookshelfMember(
  val readerId: ReaderId,
  val displayName: String,
  val role: MembershipRole)
