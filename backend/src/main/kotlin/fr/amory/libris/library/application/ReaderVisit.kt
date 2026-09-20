package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class ReaderVisit(
    private val readers: ReaderRepository,
    private val bookshelves: BookshelfRepository,
    private val transactions: TransactionOperations,
) {
    fun visit(username: String, email: String, displayName: String): Reader {
        readers.findByUsername(username)?.let { return it }
        return try {
            welcome(username, email, displayName)
        } catch (duplicate: DuplicateUsernameException) {
            readers.findByUsername(username) ?: throw duplicate
        }
    }

    private fun welcome(username: String, email: String, displayName: String): Reader {
        val readerId = ReaderId.new()
        val bookshelf = Bookshelf.ownedBy(readerId, displayName, BookshelfId.new())
        val reader = Reader(readerId, username, email, displayName, bookshelf.id)
        transactions.executeWithoutResult {
            bookshelves.insert(bookshelf)
            readers.insert(reader)
        }
        return reader
    }
}
