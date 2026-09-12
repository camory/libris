package fr.amory.libris.domain

sealed class SourceAnswer {
    data class Known(val edition: SourceEdition) : SourceAnswer()

    data object NothingKnown : SourceAnswer()

    data object Failed : SourceAnswer()
}
