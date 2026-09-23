package fr.amory.libris.bibliography.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor

@Service
class LookupEditionByIsbn(
    private val editions: EditionRepository,
    lookups: List<ExternalEditionLookup>,
) {
    private val lookups = lookups.sortedBy { it.source }

    fun lookUp(isbn: Isbn): EditionLookupResult = editions.findByIsbn(isbn)?.let { Held(it) } ?: askTheSources(isbn)

    private fun askTheSources(isbn: Isbn): EditionLookupResult {
        val results = askEveryLookup(isbn)
        val previews = results.filterIsInstance<Known>().map { it.preview }
        return when {
            previews.isNotEmpty() -> Found(previews.reduce(EditionPreview::merge))
            results.all { it == Failed } -> SourcesUnavailable
            else -> UnknownIsbn
        }
    }

    private fun askEveryLookup(isbn: Isbn): List<ExternalLookupResult> =
        newVirtualThreadPerTaskExecutor().use { executor ->
            executor.invokeAll(lookups.map { lookup -> Callable { lookup.lookUp(isbn) } }).map { it.get() }
        }
}
