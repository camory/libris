package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.series.CarriedSeries
import fr.amory.libris.bibliography.domain.series.SeriesId

class CarriedSeriesAnswering(private val seriesIds: Set<SeriesId>) : CarriedSeries {
  override fun findAll(): Set<SeriesId> =
    seriesIds
}
