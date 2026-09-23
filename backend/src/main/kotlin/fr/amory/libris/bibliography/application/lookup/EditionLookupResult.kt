package fr.amory.libris.bibliography.application.lookup

import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.lookup.EditionPreview

sealed class EditionLookupResult {
    data class Held(val edition: Edition) : EditionLookupResult()

    data class Found(val preview: EditionPreview) : EditionLookupResult()

    data object UnknownIsbn : EditionLookupResult()

    data object SourcesUnavailable : EditionLookupResult()
}
