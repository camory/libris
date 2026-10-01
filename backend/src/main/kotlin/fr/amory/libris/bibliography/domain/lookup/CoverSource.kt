package fr.amory.libris.bibliography.domain.lookup

enum class CoverSource(val label: String, val order: Int) {
  INVENTAIRE("inventaire.io", 1),
  OPEN_LIBRARY("Open Library", 2),
  BNF("BnF", 3),
}
