package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.library.application.NewBook
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Accepted
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Refused

sealed interface NewBookValidation {
    data class Accepted(val book: NewBook) : NewBookValidation

    data class Refused(val errors: List<ValidationErrorResponse>) : NewBookValidation
}

data class NewAuthorRequest(
    val name: String,
    val role: ContributionRole,
)

data class NewSeriesRequest(
    val name: String,
    val volumeNumber: Int?,
)

data class NewBookRequest(
    val isbn13: String?,
    val kind: Kind,
    val title: String,
    val subtitle: String?,
    val authors: List<NewAuthorRequest>,
    val series: NewSeriesRequest?,
    val collection: String?,
    val publisher: String?,
    val publicationYear: Int?,
    val language: String?,
    val pageCount: Int?,
    val summary: String?,
    val coverUrl: String?,
) {
    fun validate(): NewBookValidation {
        val book = NewBook.of(
            isbn = isbn13?.let { isbn13Of(it) },
            kind = kind,
            title = title,
            subtitle = subtitle,
            contributions = Contributions.of(authors.mapNotNull { Contribution.of(it.name, it.role) }),
            series = series?.let { SeriesEntry.of(it.name, it.volumeNumber) },
            collection = collection,
            publisher = publisher,
            publicationYear = publicationYear,
            language = language,
            pageCount = pageCount,
            summary = summary,
            coverUrl = coverUrl,
        )
        return if (book == null) Refused(listOf(ValidationErrorResponse("title", "blank"))) else Accepted(book)
    }
}
