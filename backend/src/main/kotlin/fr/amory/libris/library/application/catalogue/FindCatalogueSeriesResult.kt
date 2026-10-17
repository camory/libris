package fr.amory.libris.library.application.catalogue

sealed class FindCatalogueSeriesResult {
  data object NoSuchSeries : FindCatalogueSeriesResult()

  data class Found(val series: CatalogueSeries, val books: List<HeldEdition>) : FindCatalogueSeriesResult()
}
