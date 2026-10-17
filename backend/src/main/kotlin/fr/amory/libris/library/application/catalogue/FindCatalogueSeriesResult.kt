package fr.amory.libris.library.application.catalogue

sealed class FindCatalogueSeriesResult {
  data object NoSuchSeries : FindCatalogueSeriesResult()
}
