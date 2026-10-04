package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.shared.infrastructure.web.NOT_FOUND_PROBLEM
import fr.amory.libris.shared.infrastructure.web.VALIDATION_PROBLEM
import fr.amory.libris.shared.infrastructure.web.ValidationErrorResponse
import fr.amory.libris.shared.infrastructure.web.asResponse
import fr.amory.libris.shared.infrastructure.web.problem
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class IsbnController(private val lookupIsbnForReader: LookupIsbnForReader) {
  @GetMapping("/api/v1/isbn/{isbn}")
  fun isbn(@AuthenticationPrincipal reader: Reader, @PathVariable("isbn") text: String): ResponseEntity<Any> {
    val isbn = isbn13Of(text) ?: return notAnIsbn().asResponse()
    val lookup = lookupIsbnForReader(reader.id, isbn)
    return when (val result = lookup.answer) {
      is Held            -> ResponseEntity.ok(IsbnResponse.from(result, lookup.copies))
      is Found           -> ResponseEntity.ok(IsbnResponse.from(result, lookup.copies))
      UnknownIsbn        -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
      SourcesUnavailable -> problem(SERVICE_UNAVAILABLE, SOURCES_UNAVAILABLE_PROBLEM).asResponse()
    }
  }

  private fun notAnIsbn(): ProblemDetail =
    problem(BAD_REQUEST, VALIDATION_PROBLEM).apply {
      setProperty("errors", listOf(ValidationErrorResponse(field = "isbn", code = "not-an-isbn")))
    }
}

data class IsbnResponse(
  val id: String?,
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
  val covers: List<CoverCandidateResponse>,
  val copies: List<CopyResponse>) {
  companion object {
    fun from(held: Held, copies: List<CopyOnBookshelf>): IsbnResponse =
      from(held.id, held.preview, listOfNotNull(held.coverName?.let { CoverCandidateResponse.from(it) }), copies)

    fun from(found: Found, copies: List<CopyOnBookshelf>): IsbnResponse =
      from(null, found.preview, found.covers.map { CoverCandidateResponse.from(it) }, copies)

    private fun from(
      id: EditionId?,
      preview: EditionPreview,
      covers: List<CoverCandidateResponse>,
      copies: List<CopyOnBookshelf>): IsbnResponse =
      IsbnResponse(
        id = id?.value?.toString(),
        isbn13 = preview.isbn.digits,
        kind = preview.kind,
        title = preview.title,
        subtitle = preview.subtitle,
        authors = preview.contributions.map { IsbnAuthorResponse.from(it) },
        series = preview.series?.let { IsbnSeriesResponse.from(it) },
        collection = preview.collection,
        publisher = preview.publisher,
        publicationYear = preview.publicationYear,
        language = preview.language,
        pageCount = preview.pageCount,
        summary = preview.summary,
        coverUrl = covers.firstOrNull()?.url,
        covers = covers,
        copies = copies.map { CopyResponse.from(it) },
      )
  }
}

data class IsbnAuthorResponse(
  val name: String,
  val role: ContributionRole) {
  companion object {
    fun from(contribution: Contribution): IsbnAuthorResponse =
      IsbnAuthorResponse(contribution.name, contribution.role)
  }
}

data class IsbnSeriesResponse(
  val name: String,
  val volumeNumber: Int?) {
  companion object {
    fun from(series: SeriesEntry): IsbnSeriesResponse =
      IsbnSeriesResponse(series.name, series.volumeNumber)
  }
}

data class CoverCandidateResponse(
  val source: String,
  val url: String) {
  companion object {
    fun from(candidate: CoverCandidate): CoverCandidateResponse =
      CoverCandidateResponse(candidate.source.label, candidate.url)

    fun from(name: CoverName): CoverCandidateResponse =
      CoverCandidateResponse("Libris", "/api/v1/covers/${name.value}")
  }
}

private const val SOURCES_UNAVAILABLE_PROBLEM = "/problems/sources-unavailable"
