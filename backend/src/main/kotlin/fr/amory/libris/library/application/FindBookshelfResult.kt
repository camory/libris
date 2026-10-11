package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf

sealed class FindBookshelfResult {
  data class Found(val bookshelf: Bookshelf, val members: List<BookshelfMember>) : FindBookshelfResult()

  data object NoSuchBookshelf : FindBookshelfResult()
}
