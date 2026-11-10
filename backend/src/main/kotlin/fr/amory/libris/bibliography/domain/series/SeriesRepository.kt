package fr.amory.libris.bibliography.domain.series

interface SeriesRepository {
  fun findOrInsert(series: Series): Series

  fun respellOrFindOrInsert(series: Series, ownSeriesId: SeriesId?): Series

  fun findById(seriesId: SeriesId): Series?

  fun findByIds(seriesIds: List<SeriesId>): List<Series>
}
