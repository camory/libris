package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.Isbn

interface IsbnSource {
    fun lookUp(isbn: Isbn): SourceAnswer
}
