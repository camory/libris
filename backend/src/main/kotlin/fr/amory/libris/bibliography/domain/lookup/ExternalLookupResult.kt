package fr.amory.libris.bibliography.domain.lookup

sealed class ExternalLookupResult {
    data class Known(val preview: EditionPreview) : ExternalLookupResult()

    data object NothingKnown : ExternalLookupResult()

    data object Failed : ExternalLookupResult()
}
