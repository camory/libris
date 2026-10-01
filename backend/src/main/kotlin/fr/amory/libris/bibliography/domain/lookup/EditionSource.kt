package fr.amory.libris.bibliography.domain.lookup

enum class EditionSource(val precedence: Int) {
    BNF(1),
    OPEN_LIBRARY(2),
}
