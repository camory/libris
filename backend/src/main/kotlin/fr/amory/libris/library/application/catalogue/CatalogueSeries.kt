package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesId
import fr.amory.libris.bibliography.domain.cover.CoverName

data class CatalogueSeries(
  val id: SeriesId,
  val name: String,
  val volumeCount: Int,
  val coverName: CoverName?,
  val kind: Kind?)
