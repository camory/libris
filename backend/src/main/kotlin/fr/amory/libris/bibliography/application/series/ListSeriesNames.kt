package fr.amory.libris.bibliography.application.series

import fr.amory.libris.bibliography.domain.series.CarriedSeries
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import org.springframework.stereotype.Service

@Service
class ListSeriesNames(
  private val carriedSeries: CarriedSeries,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(): List<String> =
    seriesRepository
      .findByIds(carriedSeries.findAll().toList())
      .map { it.name }
}
