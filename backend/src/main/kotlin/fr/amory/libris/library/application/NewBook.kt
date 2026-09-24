package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry

data class NewBook(
    val isbn: Isbn?,
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
    companion object {
        @Suppress("LongParameterList")
        fun of(
            isbn: Isbn?,
            kind: Kind,
            title: String,
            subtitle: String?,
            contributions: Contributions,
            series: SeriesEntry?,
            collection: String?,
            publisher: String?,
            publicationYear: Int?,
            language: String?,
            pageCount: Int?,
            summary: String?,
            coverUrl: String?,
        ): NewBook? = title.takeUnless { it.isBlank() }?.let {
            NewBook(
                isbn = isbn,
                kind = kind,
                title = it,
                subtitle = subtitle,
                contributions = contributions,
                series = series,
                collection = collection,
                publisher = publisher,
                publicationYear = publicationYear,
                language = language,
                pageCount = pageCount,
                summary = summary,
                coverUrl = coverUrl,
            )
        }
    }
}
