package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.IgnoringCaseAndAccents

@JvmInline
value class BookshelfName(val value: String) {
  init {
    require(value.isNotBlank()) { "a bookshelf name is not blank" }
  }

  fun isSameAs(other: BookshelfName): Boolean =
    IgnoringCaseAndAccents.compare(value, other.value) == 0

  companion object {
    fun of(text: String): BookshelfName? =
      text.takeIf { it.isNotBlank() }?.let { BookshelfName(it) }
  }
}
