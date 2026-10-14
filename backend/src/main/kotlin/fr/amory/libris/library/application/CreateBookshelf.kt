package fr.amory.libris.library.application

import fr.amory.libris.library.application.CreateBookshelfResult.Created
import fr.amory.libris.library.application.CreateBookshelfResult.NameTaken
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations
import java.time.Clock

@Service
class CreateBookshelf(
  private val bookshelves: BookshelfRepository,
  private val transactions: TransactionOperations,
  private val clock: Clock) {
  operator fun invoke(readerId: ReaderId, name: BookshelfName): CreateBookshelfResult {
    if (bookshelves.findByMember(readerId).any { it.name.isSameAs(name) }) return NameTaken
    val bookshelf = Bookshelf.created(BookshelfId.new(), name, readerId, clock.instant())
    transactions.executeWithoutResult { bookshelves.insert(bookshelf) }
    return Created(bookshelf)
  }
}
