package fr.amory.libris.library.application

sealed class ChooseDefaultResult {
  data object Chosen : ChooseDefaultResult()

  data object NoSuchBookshelf : ChooseDefaultResult()
}
