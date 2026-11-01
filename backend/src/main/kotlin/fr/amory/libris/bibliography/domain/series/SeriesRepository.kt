package fr.amory.libris.bibliography.domain.series

interface SeriesRepository {
  fun findOrInsert(series: Series): Series

  fun findById(seriesId: SeriesId): Series?

  fun findByIds(seriesIds: List<SeriesId>): List<Series>

  fun findAll(): List<Series>
}
