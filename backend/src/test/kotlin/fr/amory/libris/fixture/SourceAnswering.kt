package fr.amory.libris.fixture

import fr.amory.libris.domain.Isbn
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.SourceAnswer

class SourceAnswering(private val answer: SourceAnswer) : IsbnSource {
    override fun lookUp(isbn: Isbn): SourceAnswer = answer
}
