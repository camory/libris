package fr.amory.libris.web.catalogue

import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.web.bookshelf.NewAuthorRequest
import fr.amory.libris.web.bookshelf.NewSeriesRequest
import fr.amory.libris.web.catalogue.EditionDetailsValidation.Accepted

data class EditionDetailsRequest(
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
  val summary: String?) {
  fun validate(): EditionDetailsValidation =
    Accepted(
      EditionDetails(
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
      ),
    )
}

sealed interface EditionDetailsValidation {
  data class Accepted(val details: EditionDetails) : EditionDetailsValidation
}
