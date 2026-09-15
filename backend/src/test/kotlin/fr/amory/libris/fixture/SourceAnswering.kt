package fr.amory.libris.fixture

import fr.amory.libris.domain.Isbn
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.SourceAnswer
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit.MILLISECONDS

class SourceAnswering(private val answer: SourceAnswer) : IsbnSource {
    override fun lookUp(isbn: Isbn): SourceAnswer = answer
}

class SourceAnsweringAtRendezvous(
    private val rendezvous: CyclicBarrier,
    private val answer: SourceAnswer,
) : IsbnSource {
    override fun lookUp(isbn: Isbn): SourceAnswer {
        rendezvous.await(WAIT_AT_MOST, MILLISECONDS)
        return answer
    }

    private companion object {
        const val WAIT_AT_MOST = 2_000L
    }
}
