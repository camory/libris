package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.domain.lookup.merge
import org.springframework.stereotype.Service

sealed class LookupResult {
    data class Found(val edition: SourceEdition, val sources: List<Source>) : LookupResult()

    data object UnknownIsbn : LookupResult()

    data object SourcesUnavailable : LookupResult()
}

@Service
class IsbnLookup(private val sources: List<IsbnSource>) {
    fun lookUp(isbn: Isbn13): LookupResult {
        val answers = sources.map { it.source to it.lookUp(isbn) }
        val known = answers.mapNotNull { (source, answer) -> knowledgeOf(source, answer) }
        return when {
            known.isNotEmpty() -> Found(merge(isbn, known.map { it.second }), known.map { it.first })
            answers.all { (_, answer) -> answer == Failed } -> SourcesUnavailable
            else -> UnknownIsbn
        }
    }

    private fun knowledgeOf(source: Source, answer: SourceAnswer): Pair<Source, SourceEdition>? =
        (answer as? Known)?.let { source to it.edition }
}
