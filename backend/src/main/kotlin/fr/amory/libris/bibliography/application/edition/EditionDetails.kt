package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry

data class EditionDetails(
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
  val summary: String?) {
  companion object {
    @Suppress("LongParameterList")
    fun of(
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
      summary: String?): EditionDetails? =
      title.takeUnless { it.isBlank() }?.let {
        EditionDetails(
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
        )
      }
  }
}
