package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesRepository

class SeriesInMemory : SeriesRepository {
  private val storedSeries = mutableListOf<Series>()

  val stored: List<Series> get() = storedSeries.toList()

  override fun findOrInsert(series: Series): Series =
    storedSeries.find { it.name.lowercase() == series.name.lowercase() } ?: series.also { storedSeries += it }

  override fun findById(seriesId: SeriesId): Series? =
    storedSeries.find { it.id == seriesId }

  override fun findByIds(seriesIds: List<SeriesId>): List<Series> =
    storedSeries.filter { it.id in seriesIds }

  override fun findAll(): List<Series> =
    stored
}
