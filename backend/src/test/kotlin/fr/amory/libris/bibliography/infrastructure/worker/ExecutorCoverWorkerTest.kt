package fr.amory.libris.bibliography.infrastructure.worker

import fr.amory.libris.bibliography.application.cover.FetchAwaitedCovers
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.AwaitedCoversInMemory
import fr.amory.libris.bibliography.fixture.CoverFetchAnswering
import fr.amory.libris.bibliography.fixture.CoversInMemory
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.coverOf
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.fixture.MutableClock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction
import java.util.concurrent.Executor

private const val ONE_PIECE = "9782723488525"

class ExecutorCoverWorkerTest {
  private val editions = EditionsInMemory()
  private val awaitedCovers = AwaitedCoversInMemory()
  private val coverFetch = CoverFetchAnswering(INVENTAIRE, coverOf("image/webp", recordedBytes("covers/small.webp")))
  private val fetchAwaitedCovers =
    FetchAwaitedCovers(
      awaitedCovers,
      listOf(coverFetch),
      CoversInMemory(),
      editions,
      withoutTransaction(),
      MutableClock(),
    )

  @Test
  fun `a waking runs the fetch of the awaited covers`() {
    // Given
    awaitingOnePiece()
    val executor = ExecutorKeeping()
    val coverWorker = ExecutorCoverWorker(fetchAwaitedCovers, executor)

    // When
    coverWorker.wake()
    executor.runKept()

    // Then
    coverFetch.asked shouldBe listOf(isbnOf(ONE_PIECE))
  }

  @Test
  fun `a waking leaves the run to the executor`() {
    // Given
    awaitingOnePiece()
    val coverWorker = ExecutorCoverWorker(fetchAwaitedCovers, ExecutorKeeping())

    // When
    coverWorker.wake()

    // Then
    coverFetch.asked.shouldBeEmpty()
  }

  private fun awaitingOnePiece() {
    editions.insert(
      Edition(
        id = EditionId.new(),
        isbn = isbnOf(ONE_PIECE),
        kind = MANGA,
        title = "Romance dawn",
        subtitle = null,
        contributions = Contributions.of(emptyList()),
        series = null,
        collection = null,
        publisher = null,
        publicationYear = null,
        language = null,
        pageCount = null,
        summary = null,
        coverName = null,
      ),
    )
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
  }

  private class ExecutorKeeping : Executor {
    private val kept = mutableListOf<Runnable>()

    override fun execute(command: Runnable) {
      kept += command
    }

    fun runKept() {
      kept.forEach { it.run() }
    }
  }
}
