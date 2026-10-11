package fr.amory.libris.library.application

import fr.amory.libris.library.application.AddMemberResult.Added
import fr.amory.libris.library.application.AddMemberResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddMemberResult.UnknownReader
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class AddMemberToBookshelf(
  private val bookshelves: BookshelfRepository,
  private val readers: ReaderRepository,
  private val transactions: TransactionOperations) {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId, membership: Membership): AddMemberResult {
    val bookshelf = bookshelves.findById(bookshelfId)
    val reader = readers.findById(membership.readerId)
    return when {
      bookshelf == null -> NoSuchBookshelf
      reader == null    -> UnknownReader
      else              -> added(bookshelf, reader, membership)
    }
  }

  private fun added(bookshelf: Bookshelf, reader: Reader, membership: Membership): AddMemberResult {
    transactions.executeWithoutResult { bookshelves.update(bookshelf.withMember(membership)) }
    return Added(BookshelfMember(reader.id, reader.displayName, membership.role))
  }
}
