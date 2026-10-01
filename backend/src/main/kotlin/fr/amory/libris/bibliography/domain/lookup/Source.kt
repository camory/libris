package fr.amory.libris.bibliography.domain.lookup

enum class Source(val label: String, val coverOrder: Int) {
    BNF("BnF", 3),
    OPEN_LIBRARY("Open Library", 2),
    INVENTAIRE("inventaire.io", 1),
}
