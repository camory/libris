package fr.amory.libris.library.domain.copy

@JvmInline
value class CatalogueQuery(val text: String) {
  companion object {
    fun of(text: String): CatalogueQuery? =
      text.takeIf { it.isNotBlank() }?.let { CatalogueQuery(it) }
  }
}
