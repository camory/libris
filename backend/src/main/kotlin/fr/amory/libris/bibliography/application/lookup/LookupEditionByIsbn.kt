package fr.amory.libris.bibliography.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.CoverCandidates
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalCoverLookup
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
    private val coverLookup: ExternalCoverLookup,
) {
    private val lookups = lookups.sortedBy { it.source }

    operator fun invoke(isbn: Isbn): EditionLookupResult = editions.findByIsbn(isbn)?.let(::held) ?: askTheSources(isbn)

    private fun held(edition: Edition): Held? = EditionPreview.of(edition)?.let { Held(edition.id, it) }

    private fun askTheSources(isbn: Isbn): EditionLookupResult =
        newVirtualThreadPerTaskExecutor().use { executor ->
            val picture = executor.submit(Callable { coverLookup.lookUp(isbn) })
            val results = executor.invokeAll(lookups.map { lookup -> Callable { lookup.lookUp(isbn) } })
                .map { it.get() }
            answerOf(results, picture.get())
        }

    private fun answerOf(results: List<ExternalLookupResult>, picture: CoverCandidate?): EditionLookupResult {
        val previews = results.filterIsInstance<Known>().map { it.preview }
        return when {
            previews.isNotEmpty() -> Found(previews.reduce(EditionPreview::merge), coversOf(results, picture))
            results.all { it == Failed } -> SourcesUnavailable
            else -> UnknownIsbn
        }
    }

    private fun coversOf(results: List<ExternalLookupResult>, picture: CoverCandidate?): CoverCandidates =
        CoverCandidates.of(
            listOfNotNull(picture) + lookups.zip(results).mapNotNull { (lookup, result) ->
                (result as? Known)?.preview?.coverUrl?.let { CoverCandidate(lookup.source, it) }
            },
        )
}
