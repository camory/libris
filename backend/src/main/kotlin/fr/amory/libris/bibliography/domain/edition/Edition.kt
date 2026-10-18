package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.series.SeriesVolume

data class Edition(
  val id: EditionId,
  val isbn: Isbn?,
  val kind: Kind,
  val title: String,
  val subtitle: String?,
  val contributions: Contributions,
  val seriesVolume: SeriesVolume?,
  val collection: String?,
  val publisher: String?,
  val publicationYear: Int?,
  val language: String?,
  val pageCount: Int?,
  val summary: String?,
  val coverName: CoverName?) {
  init {
    require(title.isNotBlank()) { "an edition needs a title" }
  }
}
