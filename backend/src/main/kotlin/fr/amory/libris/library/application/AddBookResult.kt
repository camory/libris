package fr.amory.libris.library.application

import fr.amory.libris.library.application.lookup.CopyOnBookshelf

sealed class AddBookResult {
  data class Added(val copy: CopyOnBookshelf) : AddBookResult()

  data object NoSuchBookshelf : AddBookResult()

  data object NotAnOwner : AddBookResult()
}
