package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.series.SeriesId

data class CatalogueSeries(
  val id: SeriesId,
  val name: String,
  val volumeCount: Int,
  val coverName: CoverName?,
  val kind: Kind?)
