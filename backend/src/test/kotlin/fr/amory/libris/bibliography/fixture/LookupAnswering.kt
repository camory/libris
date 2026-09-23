package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult
import fr.amory.libris.bibliography.domain.lookup.Source
import fr.amory.libris.bibliography.domain.lookup.Source.BNF
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit.MILLISECONDS

class LookupAnswering(
    private val answer: ExternalLookupResult,
    override val source: Source = BNF,
) : ExternalEditionLookup {
    private val isbns = mutableListOf<Isbn>()

    val asked: List<Isbn> get() = isbns.toList()

    override fun lookUp(isbn: Isbn): ExternalLookupResult {
        isbns += isbn
        return answer
    }
}

class LookupAnsweringAtRendezvous(
    private val rendezvous: CyclicBarrier,
    private val answer: ExternalLookupResult,
    override val source: Source = BNF,
) : ExternalEditionLookup {
    override fun lookUp(isbn: Isbn): ExternalLookupResult {
        rendezvous.await(WAIT_AT_MOST, MILLISECONDS)
        return answer
    }

    private companion object {
        const val WAIT_AT_MOST = 2_000L
    }
}
