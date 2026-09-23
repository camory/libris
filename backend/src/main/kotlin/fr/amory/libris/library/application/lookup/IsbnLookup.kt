package fr.amory.libris.library.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult

data class IsbnLookup(
    val answer: EditionLookupResult,
    val copies: List<CopyOnBookshelf>,
)
