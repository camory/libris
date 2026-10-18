package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.cover.CoverWorker
import fr.amory.libris.bibliography.application.edition.HoldEdition
import fr.amory.libris.bibliography.application.edition.NewEdition
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations
import java.time.Clock

@Service
class AddBookToBookshelf(
  private val holdEdition: HoldEdition,
  private val copies: CopyRepository,
  private val bookshelves: BookshelfRepository,
  private val transactions: TransactionOperations,
  private val coverWorker: CoverWorker,
  private val clock: Clock) {
  operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId, newEdition: NewEdition): AddBookResult {
    val bookshelf = bookshelves.findById(bookshelfId)?.takeIf { it.hasMember(readerId) }
    return when {
      bookshelf == null              -> NoSuchBookshelf
      !bookshelf.isOwnedBy(readerId) -> NotAnOwner
      else                           -> added(readerId, bookshelf, newEdition)
    }
  }

  private fun added(readerId: ReaderId, bookshelf: Bookshelf, newEdition: NewEdition): AddBookResult {
    val copy = transactions.execute {
      val edition = holdEdition(newEdition)
      Copy(CopyId.new(), edition.id, bookshelf.id, readerId, clock.instant()).also { copies.insert(it) }
    }
    coverWorker.wake()
    return Added(copy, bookshelf)
  }
}
