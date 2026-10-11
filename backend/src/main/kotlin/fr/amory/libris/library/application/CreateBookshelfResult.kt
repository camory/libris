package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf

sealed class CreateBookshelfResult {
  data class Created(val bookshelf: Bookshelf) : CreateBookshelfResult()

  data object BlankName : CreateBookshelfResult()

  data object NameTaken : CreateBookshelfResult()
}
