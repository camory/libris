package fr.amory.libris.web.bookshelf

import fr.amory.libris.library.domain.bookshelf.BookshelfName

data class NewBookshelfRequest(val name: String) {
  fun validate(): BookshelfName? =
    BookshelfName.of(name)
}
