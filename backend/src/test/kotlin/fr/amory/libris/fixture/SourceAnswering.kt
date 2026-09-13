package fr.amory.libris.fixture

import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceAnswer

class SourceAnswering(override val source: Source, private val answer: SourceAnswer) : IsbnSource {
    override fun lookUp(isbn: Isbn13): SourceAnswer = answer
}
