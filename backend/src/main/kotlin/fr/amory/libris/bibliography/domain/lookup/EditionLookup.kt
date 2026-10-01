package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface EditionLookup {
    val source: Source

    fun lookUp(isbn: Isbn): EditionSourceAnswer
}
