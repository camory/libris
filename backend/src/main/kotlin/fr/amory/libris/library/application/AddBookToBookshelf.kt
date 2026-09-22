package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.CopyRepository
import org.springframework.stereotype.Service

@Service
class AddBookToBookshelf(
    private val editions: EditionRepository,
    private val copies: CopyRepository,
    private val bookshelves: BookshelfRepository,
) {
    fun add(bookshelf: BookshelfId, book: NewBook): AddBookResult {
        val shelf = bookshelves.findById(bookshelf) ?: return NoSuchBookshelf
        val edition = editionFor(book)
        val copy = Copy(CopyId.new(), edition.id, shelf.id)
        copies.insert(copy)
        return Added(copy, shelf)
    }

    private fun editionFor(book: NewBook): Edition {
        val isbn = book.isbn13?.let { Isbn.ofThirteen(it) }
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
