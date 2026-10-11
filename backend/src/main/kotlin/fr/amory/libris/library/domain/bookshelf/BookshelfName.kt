package fr.amory.libris.library.domain.bookshelf

@JvmInline
value class BookshelfName(val value: String) {
  init {
    require(value.isNotBlank()) { "a bookshelf name is not blank" }
  }

  companion object {
    fun of(text: String): BookshelfName? =
      text.takeIf { it.isNotBlank() }?.let { BookshelfName(it) }
  }
}
