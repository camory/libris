package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnIsbn
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class AddBookToBookshelf(
    private val editions: EditionRepository,
    private val copies: CopyRepository,
    private val bookshelves: BookshelfRepository,
) {
    fun add(reader: ReaderId, bookshelf: BookshelfId, book: NewBook): AddBookResult {
        val shelf = bookshelves
            .findById(bookshelf)
            ?.takeIf { found -> found.memberships.any { it.readerId == reader } }
        val isbn = book.isbn13?.let { Isbn.ofThirteen(it) }
        return when {
            shelf == null -> NoSuchBookshelf
            book.isbn13 != null && isbn == null -> NotAnIsbn
            else -> added(shelf, book, isbn)
        }
    }

    private fun added(shelf: Bookshelf, book: NewBook, isbn: Isbn?): AddBookResult {
        val edition = editionFor(book, isbn)
        val copy = Copy(CopyId.new(), edition.id, shelf.id)
        copies.insert(copy)
        return Added(copy, shelf)
    }

    private fun editionFor(book: NewBook, isbn: Isbn?): Edition {
        isbn?.let { editions.findByIsbn(it) }?.let { return it }
        return newEdition(book, isbn).also { editions.insert(it) }
    }

    private fun newEdition(book: NewBook, isbn: Isbn?): Edition = Edition(
        id = EditionId.new(),
        isbn = isbn,
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
