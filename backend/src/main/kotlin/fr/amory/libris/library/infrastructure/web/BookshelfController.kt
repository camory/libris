package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.NewBook
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class BookshelfResponse(
    val id: String,
    val name: String,
)

data class CopyResponse(
    val id: String,
    val bookshelf: BookshelfResponse,
)

@RestController
class BookshelfController(private val addBookToBookshelf: AddBookToBookshelf) {
    @PostMapping("/api/v1/bookshelves/{id}/books")
    fun add(
        @AuthenticationPrincipal reader: Reader,
        @PathVariable("id") id: UUID,
        @RequestBody request: NewBookRequest,
    ): ResponseEntity<Any> {
        val isbn = request.isbn13?.let { isbn13Of(it) }
        val contributions = request.authors.map { Contribution.of(it.name, it.role) }
        val series = request.series?.let { SeriesEntry.of(it.name, it.volumeNumber) }
        val errors = buildList {
            if (request.isbn13 != null && isbn == null) add(ValidationErrorResponse("isbn13", "not-an-isbn"))
            if (request.title.isBlank()) add(ValidationErrorResponse("title", "blank"))
            if (null in contributions) add(ValidationErrorResponse("authors", "blank"))
            if (request.series != null && series == null) add(ValidationErrorResponse("series", "blank"))
        }
        if (errors.isNotEmpty()) return invalid(errors).asResponse()
        val book = bookOf(request, isbn, Contributions.of(contributions.filterNotNull()), series)
        return when (val result = addBookToBookshelf(reader.id, BookshelfId(id), book)) {
            is Added -> ResponseEntity.status(CREATED).body(
                CopyResponse(
                    id = result.copy.id.value.toString(),
                    bookshelf = BookshelfResponse(result.bookshelf.id.value.toString(), result.bookshelf.name),
                ),
            )
            NoSuchBookshelf, NotAnOwner -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
        }
    }

    private fun invalid(errors: List<ValidationErrorResponse>): ProblemDetail =
        problem(BAD_REQUEST, VALIDATION_PROBLEM).apply { setProperty("errors", errors) }

    private fun bookOf(
        request: NewBookRequest,
        isbn: Isbn?,
        contributions: Contributions,
        series: SeriesEntry?,
    ): NewBook = NewBook(
        isbn = isbn,
        kind = request.kind,
        title = request.title,
        subtitle = request.subtitle,
        contributions = contributions,
        series = series,
        collection = request.collection,
        publisher = request.publisher,
        publicationYear = request.publicationYear,
        language = request.language,
        pageCount = request.pageCount,
        summary = request.summary,
        coverUrl = request.coverUrl,
    )
}
