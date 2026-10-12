package fr.amory.libris.library.application

import fr.amory.libris.library.application.ChooseDefaultResult.Chosen
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class ChooseDefaultBookshelf(
  private val readers: ReaderRepository,
  private val transactions: TransactionOperations) {
  operator fun invoke(reader: Reader, bookshelfId: BookshelfId): ChooseDefaultResult {
    transactions.executeWithoutResult { readers.update(reader.copy(defaultBookshelfId = bookshelfId)) }
    return Chosen
  }
}
