package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId

data class CatalogueSeries(
  val id: SeriesId,
  val name: String,
  val volumeCount: Int,
  val coverName: CoverName?,
  val kind: Kind?)

internal fun catalogueSeriesOf(series: Series, seriesEditions: List<Edition>, firstOfEachSeries: Set<EditionId>) =
  CatalogueSeries(
    series.id,
    series.name,
    seriesEditions.size,
    coverName = seriesEditions.firstOrNull { it.id in firstOfEachSeries }?.coverName,
    kind = seriesEditions.map { it.kind }.distinct().singleOrNull(),
  )
