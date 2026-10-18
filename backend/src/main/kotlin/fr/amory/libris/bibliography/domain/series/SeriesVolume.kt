package fr.amory.libris.bibliography.domain.series

import fr.amory.libris.bibliography.domain.SeriesEntry

data class SeriesVolume(
  val seriesId: SeriesId,
  val volumeNumber: Int?) {
  fun entryIn(series: Series): SeriesEntry {
    require(series.id == seriesId) { "a volume enters its own series only" }
    return SeriesEntry(series.name, volumeNumber)
  }
}
