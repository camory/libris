package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.ReaderId

data class ReaderBookshelf(
  val id: BookshelfId,
  val name: String,
  val role: MembershipRole) {
  companion object {
    fun of(bookshelf: Bookshelf, readerId: ReaderId): ReaderBookshelf? =
      bookshelf
        .roleOf(readerId)
        ?.let { ReaderBookshelf(bookshelf.id, bookshelf.name.value, it) }
  }
}
