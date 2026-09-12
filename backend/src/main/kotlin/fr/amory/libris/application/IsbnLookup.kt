package fr.amory.libris.application

import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.stereotype.Service

sealed class LookupResult {
    data class Found(val edition: SourceEdition, val sources: List<Source>) : LookupResult()
}

@Service
class IsbnLookup(private val source: IsbnSource) {
    fun lookUp(isbn: Isbn13): LookupResult = when (val answer = source.lookUp(isbn)) {
        is Known -> LookupResult.Found(answer.edition, listOf(source.source))
        else -> TODO()
    }
}
