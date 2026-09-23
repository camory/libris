package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
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

@Service
class AddBookToBookshelf(
    private val editions: EditionRepository,
    private val copies: CopyRepository,
    private val bookshelves: BookshelfRepository,
    private val transactions: TransactionOperations,
) {
    operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId, book: NewBook): AddBookResult {
        val bookshelf = bookshelves.findById(bookshelfId)?.takeIf { it.hasMember(readerId) }
        return when {
            bookshelf == null -> NoSuchBookshelf
            !bookshelf.isOwnedBy(readerId) -> NotAnOwner
            else -> added(bookshelf, book)
        }
    }

    private fun added(bookshelf: Bookshelf, book: NewBook): AddBookResult {
        val heldEdition = book.isbn?.let { editions.findByIsbn(it) }
        val edition = heldEdition ?: newEdition(book)
        val copy = Copy(CopyId.new(), edition.id, bookshelf.id)
        transactions.executeWithoutResult {
            if (heldEdition == null) editions.insert(edition)
            copies.insert(copy)
        }
        return Added(copy, bookshelf)
    }

    private fun newEdition(book: NewBook): Edition = Edition(
        id = EditionId.new(),
        isbn = book.isbn,
        kind = book.kind,
        title = book.title,
        subtitle = book.subtitle,
        contributions = book.contributions,
        series = book.series,
        collection = book.collection,
        publisher = book.publisher,
        publicationYear = book.publicationYear,
        language = book.language,
        pageCount = book.pageCount,
        summary = book.summary,
        coverUrl = book.coverUrl,
    )
}
