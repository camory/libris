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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

private const val ONE_PIECE = "9782723488525"

class ExecutorCoverWorkerTest {
  private val editions = EditionsInMemory()
  private val awaitedCovers = AwaitedCoversInMemory()
  private val coverFetch = CoverFetchAnswering(coverOf("image/webp", recordedBytes("covers/small.webp")))
  private val fetchAwaitedCovers =
    FetchAwaitedCovers(awaitedCovers, coverFetch, CoversInMemory(), editions, withoutTransaction())

  @Test
  fun `a waking runs the fetch of the awaited covers`() {
    // Given
    awaitingOnePiece()
    val coverWorker = ExecutorCoverWorker(fetchAwaitedCovers)

    // When
    coverWorker.wake()

    // Then
    coverFetch.asked shouldBe listOf(isbnOf(ONE_PIECE))
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
}
