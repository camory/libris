package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.Isbn

@JvmInline
value class CatalogueQuery(val text: String) {
  init {
    require(text.isNotBlank()) { "a catalogue query is not blank" }
  }

  val isbn: Isbn?
    get() = Isbn.of(text)

  companion object {
    fun of(text: String): CatalogueQuery? =
      text.takeIf { it.isNotBlank() }?.let { CatalogueQuery(it) }
  }
}
