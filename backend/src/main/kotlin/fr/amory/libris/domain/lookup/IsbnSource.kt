package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.Isbn13

interface IsbnSource {
    val source: Source

    fun lookUp(isbn: Isbn13): SourceAnswer
}
