package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface ExternalEditionLookup {
    fun lookUp(isbn: Isbn): ExternalLookupResult
}
