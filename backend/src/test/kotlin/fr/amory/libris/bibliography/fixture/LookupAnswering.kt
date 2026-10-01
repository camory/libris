package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.CoverLookup
import fr.amory.libris.bibliography.domain.lookup.EditionLookup
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer
import fr.amory.libris.bibliography.domain.lookup.Source
import fr.amory.libris.bibliography.domain.lookup.Source.BNF
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val WAIT_AT_MOST = 2_000L

class LookupAnswering(
    private val answer: EditionSourceAnswer,
    override val source: Source = BNF,
) : EditionLookup {
    private val isbns = mutableListOf<Isbn>()

    val asked: List<Isbn> get() = isbns.toList()

    override fun lookUp(isbn: Isbn): EditionSourceAnswer {
        isbns += isbn
        return answer
    }
}

class CoverLookupAnswering(private val candidate: CoverCandidate?) : CoverLookup {
    private val isbns = mutableListOf<Isbn>()

    val asked: List<Isbn> get() = isbns.toList()

    override fun lookUp(isbn: Isbn): CoverCandidate? {
        isbns += isbn
        return candidate
    }
}

class LookupAnsweringAtRendezvous(
    private val rendezvous: CyclicBarrier,
    private val answer: EditionSourceAnswer,
    override val source: Source = BNF,
) : EditionLookup {
    override fun lookUp(isbn: Isbn): EditionSourceAnswer {
        rendezvous.await(WAIT_AT_MOST, MILLISECONDS)
        return answer
    }
}

class CoverLookupAnsweringAtRendezvous(
    private val rendezvous: CyclicBarrier,
    private val candidate: CoverCandidate?,
) : CoverLookup {
    override fun lookUp(isbn: Isbn): CoverCandidate? {
        rendezvous.await(WAIT_AT_MOST, MILLISECONDS)
        return candidate
    }
}
