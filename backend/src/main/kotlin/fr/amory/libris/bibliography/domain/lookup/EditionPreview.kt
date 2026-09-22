package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry

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
        contributions = if (contributions.all.isEmpty()) other.contributions else contributions,
        series = series ?: other.series,
        collection = collection ?: other.collection,
        publisher = publisher ?: other.publisher,
        publicationYear = publicationYear ?: other.publicationYear,
        language = language ?: other.language,
        pageCount = pageCount ?: other.pageCount,
        summary = summary ?: other.summary,
        coverUrl = coverUrl ?: other.coverUrl,
    )
}
