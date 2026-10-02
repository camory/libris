package fr.amory.libris.bibliography.domain.cover

enum class CoverSource(val label: String, val order: Int) {
  INVENTAIRE("inventaire.io", 1),
  OPEN_LIBRARY("Open Library", 2),
  BNF("BnF", 3);

  companion object {
    fun of(label: String): CoverSource? =
      entries.firstOrNull { it.label == label }
  }
}
