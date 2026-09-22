package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.library.domain.lookup.CopyOnBookshelf

sealed class IsbnLookupResult {
    data class Held(val edition: Edition, val copies: List<CopyOnBookshelf>) : IsbnLookupResult()

    data class NotHeld(val sources: EditionLookupResult) : IsbnLookupResult()
}
