package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.library.domain.lookup.CopyOnBookshelf

sealed class IsbnLookupResult {
    data class Found(val preview: EditionPreview, val copies: List<CopyOnBookshelf>) : IsbnLookupResult()

    data object UnknownIsbn : IsbnLookupResult()

    data object SourcesUnavailable : IsbnLookupResult()
}
