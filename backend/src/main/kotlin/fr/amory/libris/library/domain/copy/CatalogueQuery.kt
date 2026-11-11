package fr.amory.libris.library.domain.copy

@JvmInline
value class CatalogueQuery(val text: String) {
  init {
    require(text.isNotBlank()) { "a catalogue query is not blank" }
  }

  companion object {
    fun of(text: String): CatalogueQuery? =
      text.takeIf { it.isNotBlank() }?.let { CatalogueQuery(it) }
  }
}
