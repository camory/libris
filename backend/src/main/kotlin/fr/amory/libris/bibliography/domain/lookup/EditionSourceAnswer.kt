package fr.amory.libris.bibliography.domain.lookup

sealed class EditionSourceAnswer {
    data class Known(val preview: EditionPreview) : EditionSourceAnswer()

    data object NothingKnown : EditionSourceAnswer()

    data object Failed : EditionSourceAnswer()
}
