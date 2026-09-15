package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.Isbn
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.domain.lookup.merge
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor

sealed class LookupResult {
    data class Found(val edition: SourceEdition) : LookupResult()

    data object UnknownIsbn : LookupResult()

    data object SourcesUnavailable : LookupResult()
}

@Service
class IsbnLookup(private val sources: List<IsbnSource>) {
    fun lookUp(isbn: Isbn): LookupResult {
        val answers = askEverySource(isbn)
        val editions = answers.filterIsInstance<Known>().map { it.edition }
        return when {
            editions.isNotEmpty() -> Found(merge(editions))
            answers.all { it == Failed } -> SourcesUnavailable
            else -> UnknownIsbn
        }
    }

    private fun askEverySource(isbn: Isbn): List<SourceAnswer> =
        newVirtualThreadPerTaskExecutor().use { executor ->
            executor.invokeAll(sources.map { source -> Callable { source.lookUp(isbn) } }).map { it.get() }
        }
}
