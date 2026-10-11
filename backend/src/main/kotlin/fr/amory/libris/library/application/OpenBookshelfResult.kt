package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf

sealed class OpenBookshelfResult {
  data class Opened(val bookshelf: Bookshelf, val members: List<BookshelfMember>) : OpenBookshelfResult()

  data object NoSuchBookshelf : OpenBookshelfResult()
}
