package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.SeriesId
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.NoSuchSeries
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class FindCatalogueSeries {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, seriesId: SeriesId): FindCatalogueSeriesResult =
    NoSuchSeries
}
