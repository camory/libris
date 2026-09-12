package fr.amory.libris.infra.web

import fr.amory.libris.application.IsbnLookup
import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.AuthorRole
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URI

private const val VALIDATION_PROBLEM = "/problems/validation"
private const val NOT_FOUND_PROBLEM = "/problems/not-found"
private const val SOURCES_UNAVAILABLE_PROBLEM = "/problems/sources-unavailable"

data class ValidationErrorResponse(
    val field: String,
    val code: String,
)

data class IsbnAuthorResponse(
    val name: String,
    val role: AuthorRole,
)

data class IsbnSeriesResponse(
    val name: String,
    val volumeNumber: Int?,
)

data class IsbnResponse(
    val isbn13: String,
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
    val sources: List<Source>,
)

private fun problem(status: HttpStatus, type: String): ProblemDetail =
    ProblemDetail.forStatus(status).apply {
        this.type = URI.create(type)
        this.title = status.reasonPhrase
    }

private fun ProblemDetail.asResponse(): ResponseEntity<Any> = ResponseEntity.status(status).body(this)

@RestController
class IsbnController(private val lookup: IsbnLookup) {
    @GetMapping("/api/v1/isbn/{isbn}")
    fun isbn(@PathVariable isbn: String): ResponseEntity<Any> {
        val isbn13 = Isbn13.of(isbn) ?: return notAnIsbn().asResponse()
        return when (val result = lookup.lookUp(isbn13)) {
            is Found -> ResponseEntity.ok(responseOf(result.edition, result.sources))
            UnknownIsbn -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
            SourcesUnavailable -> problem(SERVICE_UNAVAILABLE, SOURCES_UNAVAILABLE_PROBLEM).asResponse()
        }
    }

    private fun notAnIsbn(): ProblemDetail = problem(BAD_REQUEST, VALIDATION_PROBLEM).apply {
        setProperty("errors", listOf(ValidationErrorResponse(field = "isbn", code = "not-an-isbn")))
    }

    private fun responseOf(edition: SourceEdition, sources: List<Source>): IsbnResponse = IsbnResponse(
        isbn13 = edition.isbn13.digits,
        title = edition.title,
        subtitle = edition.subtitle,
        authors = edition.authors.map { IsbnAuthorResponse(it.name, it.role) },
        series = edition.series?.let { IsbnSeriesResponse(it.name, it.volumeNumber) },
        collection = edition.collection,
        publisher = edition.publisher,
        publicationYear = edition.publicationYear,
        language = edition.language,
        pageCount = edition.pageCount,
        summary = edition.summary,
        coverUrl = edition.coverUrl,
        sources = sources,
    )
}
