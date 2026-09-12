package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.stereotype.Service

sealed class LookupResult {
    data class Found(val edition: SourceEdition, val sources: List<Source>) : LookupResult()

    data object UnknownIsbn : LookupResult()

    data object SourcesUnavailable : LookupResult()
}

@Service
class IsbnLookup(private val source: IsbnSource) {
    fun lookUp(isbn: Isbn13): LookupResult = when (val answer = source.lookUp(isbn)) {
        is Known -> Found(answer.edition, listOf(source.source))
        NothingKnown -> UnknownIsbn
        Failed -> SourcesUnavailable
    }
}
