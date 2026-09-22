package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.library.application.IsbnLookupResult.Found
import fr.amory.libris.library.application.IsbnLookupResult.SourcesUnavailable
import fr.amory.libris.library.application.IsbnLookupResult.UnknownIsbn
import org.springframework.stereotype.Service

@Service
class LookupIsbnForReader(
    private val editions: EditionRepository,
    private val lookup: LookupEditionByIsbn,
) {
    fun lookUp(isbn: Isbn): IsbnLookupResult =
        editions.findByIsbn(isbn)?.let { Found(previewOf(it, isbn), emptyList()) } ?: askTheSources(isbn)

    private fun askTheSources(isbn: Isbn): IsbnLookupResult = when (val answer = lookup.lookUp(isbn)) {
        is EditionLookupResult.Found -> Found(answer.preview, emptyList())
        EditionLookupResult.UnknownIsbn -> UnknownIsbn
        EditionLookupResult.SourcesUnavailable -> SourcesUnavailable
    }

    private fun previewOf(edition: Edition, isbn: Isbn): EditionPreview = EditionPreview(
        isbn = isbn,
        kind = edition.kind,
        title = edition.title,
        subtitle = edition.subtitle,
        contributions = edition.contributions,
        series = edition.series,
        collection = edition.collection,
        publisher = edition.publisher,
        publicationYear = edition.publicationYear,
        language = edition.language,
        pageCount = edition.pageCount,
        summary = edition.summary,
        coverUrl = edition.coverUrl,
    )
}
