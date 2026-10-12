package fr.amory.libris.library.application

import fr.amory.libris.library.application.ChooseDefaultResult.Chosen
import fr.amory.libris.library.application.ChooseDefaultResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class ChooseDefaultBookshelf(
  private val readers: ReaderRepository,
  private val bookshelves: BookshelfRepository,
  private val transactions: TransactionOperations) {
  operator fun invoke(reader: Reader, bookshelfId: BookshelfId): ChooseDefaultResult {
    val bookshelf = bookshelves.findById(bookshelfId)?.takeIf { it.isOwnedBy(reader.id) }
    return when (bookshelf) {
      null -> NoSuchBookshelf
      else -> chosen(reader, bookshelfId)
    }
  }

  private fun chosen(reader: Reader, bookshelfId: BookshelfId): ChooseDefaultResult {
    transactions.executeWithoutResult { readers.update(reader.copy(defaultBookshelfId = bookshelfId)) }
    return Chosen
  }
}
