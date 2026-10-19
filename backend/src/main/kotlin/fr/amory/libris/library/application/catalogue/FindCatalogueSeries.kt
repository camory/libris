package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.Found
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.NoSuchSeries
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class FindCatalogueSeries(
  private val listCatalogueSeries: ListCatalogueSeries) {
  operator fun invoke(readerId: ReaderId, seriesId: SeriesId): FindCatalogueSeriesResult {
    val series = listCatalogueSeries(readerId).find { it.id == seriesId } ?: return NoSuchSeries
    return Found(series, emptyList())
  }
}
