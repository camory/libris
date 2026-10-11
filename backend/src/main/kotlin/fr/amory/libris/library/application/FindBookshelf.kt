package fr.amory.libris.library.application

import fr.amory.libris.library.application.FindBookshelfResult.Found
import fr.amory.libris.library.application.FindBookshelfResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.stereotype.Service

@Service
class FindBookshelf(
  private val bookshelves: BookshelfRepository,
  private val readers: ReaderRepository) {
  operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId): FindBookshelfResult =
    bookshelves
      .findById(bookshelfId)
      ?.takeIf { it.hasMember(readerId) }
      ?.let { Found(it, membersOf(it)) }
      ?: NoSuchBookshelf

  private fun membersOf(bookshelf: Bookshelf): List<BookshelfMember> =
    readers
      .findByIds(bookshelf.memberships.map { it.readerId })
      .mapNotNull { memberOf(bookshelf, it) }
      .sortedBy { it.role.order }

  private fun memberOf(bookshelf: Bookshelf, reader: Reader): BookshelfMember? =
    bookshelf
      .roleOf(reader.id)
      ?.let { BookshelfMember(reader.id, reader.displayName, it) }
}
