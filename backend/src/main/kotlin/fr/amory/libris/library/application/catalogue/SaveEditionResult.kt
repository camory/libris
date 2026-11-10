package fr.amory.libris.library.application.catalogue

sealed class SaveEditionResult {
  data class Saved(val held: HeldEdition) : SaveEditionResult()

  data object NoSuchEdition : SaveEditionResult()
}
