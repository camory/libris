package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface ExternalEditionLookup {
    val source: Source

    fun lookUp(isbn: Isbn): ExternalLookupResult
}
