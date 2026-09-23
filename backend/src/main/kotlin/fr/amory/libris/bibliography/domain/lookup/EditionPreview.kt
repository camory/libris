package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition

data class EditionPreview(
    val isbn: Isbn,
    val kind: Kind,
    val title: String,
    val subtitle: String?,
    val contributions: Contributions,
    val series: SeriesEntry?,
    val collection: String?,
    val publisher: String?,
    val publicationYear: Int?,
    val language: String?,
    val pageCount: Int?,
    val summary: String?,
    val coverUrl: String?,
) {
    fun merge(other: EditionPreview): EditionPreview = copy(
        subtitle = subtitle ?: other.subtitle,
        contributions = if (contributions.isEmpty()) other.contributions else contributions,
        series = series ?: other.series,
        collection = collection ?: other.collection,
        publisher = publisher ?: other.publisher,
        publicationYear = publicationYear ?: other.publicationYear,
        language = language ?: other.language,
        pageCount = pageCount ?: other.pageCount,
        summary = summary ?: other.summary,
        coverUrl = coverUrl ?: other.coverUrl,
    )

    companion object {
        fun of(edition: Edition): EditionPreview? = edition.isbn?.let {
            EditionPreview(
                isbn = it,
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
    }
}
