package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.Isbn

interface IsbnSource {
    val source: Source

    fun lookUp(isbn: Isbn): SourceAnswer
}
