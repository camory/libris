package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.MembershipRole

data class ReaderBookshelf(
  val id: BookshelfId,
  val name: String,
  val role: MembershipRole)
