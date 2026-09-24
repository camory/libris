package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

private const val SOURCES_UNAVAILABLE_PROBLEM = "/problems/sources-unavailable"

data class IsbnAuthorResponse(
    val name: String,
    val role: ContributionRole,
)

data class IsbnSeriesResponse(
    val name: String,
    val volumeNumber: Int?,
)

data class IsbnResponse(
    val isbn13: String,
    val kind: Kind,
    val title: String,
    val subtitle: String?,
    val authors: List<IsbnAuthorResponse>,
    val series: IsbnSeriesResponse?,
    val collection: String?,
    val publisher: String?,
    val publicationYear: Int?,
    val language: String?,
    val pageCount: Int?,
    val summary: String?,
    val coverUrl: String?,
    val copies: List<CopyResponse>,
)

@RestController
class IsbnController(private val lookupIsbnForReader: LookupIsbnForReader) {
    @GetMapping("/api/v1/isbn/{isbn}")
    fun isbn(@AuthenticationPrincipal reader: Reader, @PathVariable("isbn") text: String): ResponseEntity<Any> {
        val isbn = isbn13Of(text) ?: return notAnIsbn().asResponse()
        val lookup = lookupIsbnForReader(reader.id, isbn)
        return when (val result = lookup.answer) {
            is Held -> ResponseEntity.ok(responseOf(result.preview, lookup.copies))
            is Found -> ResponseEntity.ok(responseOf(result.preview, lookup.copies))
            UnknownIsbn -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
            SourcesUnavailable -> problem(SERVICE_UNAVAILABLE, SOURCES_UNAVAILABLE_PROBLEM).asResponse()
        }
    }

    private fun notAnIsbn(): ProblemDetail = problem(BAD_REQUEST, VALIDATION_PROBLEM).apply {
        setProperty("errors", listOf(ValidationErrorResponse(field = "isbn", code = "not-an-isbn")))
    }

    private fun responseOf(preview: EditionPreview, copies: List<CopyOnBookshelf>): IsbnResponse = IsbnResponse(
        isbn13 = preview.isbn.digits,
        kind = preview.kind,
        title = preview.title,
        subtitle = preview.subtitle,
        authors = preview.contributions.map { IsbnAuthorResponse(it.name, it.role) },
        series = preview.series?.let { IsbnSeriesResponse(it.name, it.volumeNumber) },
        collection = preview.collection,
        publisher = preview.publisher,
        publicationYear = preview.publicationYear,
        language = preview.language,
        pageCount = preview.pageCount,
        summary = preview.summary,
        coverUrl = preview.coverUrl,
        copies = copies.map { responseOf(it) },
    )

    private fun responseOf(copy: CopyOnBookshelf): CopyResponse = CopyResponse(
        id = copy.copyId.value.toString(),
        bookshelf = BookshelfResponse(copy.bookshelfId.value.toString(), copy.bookshelfName),
    )
}
