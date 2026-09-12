package fr.amory.libris.infra.web

import fr.amory.libris.application.IsbnLookup
import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.domain.AuthorRole
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

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

@RestController
class IsbnController(private val lookup: IsbnLookup) {
    @GetMapping("/api/v1/isbn/{isbn}")
    fun isbn(@PathVariable isbn: String): IsbnResponse {
        val isbn13 = Isbn13.of(isbn) ?: TODO()
        return when (val result = lookup.lookUp(isbn13)) {
            is Found -> responseOf(result.edition, result.sources)
            else -> TODO()
        }
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
