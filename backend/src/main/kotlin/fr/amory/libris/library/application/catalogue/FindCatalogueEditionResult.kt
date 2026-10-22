package fr.amory.libris.library.application.catalogue

sealed class FindCatalogueEditionResult {
  data class Found(val held: HeldEdition) : FindCatalogueEditionResult()

  data object NoSuchEdition : FindCatalogueEditionResult()
}
