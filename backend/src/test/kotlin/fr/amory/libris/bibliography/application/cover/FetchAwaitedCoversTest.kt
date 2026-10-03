package fr.amory.libris.bibliography.application.cover

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.CoverSource
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

class FetchAwaitedCoversTest {
  private val editions = EditionsInMemory()
  private val awaitedCovers = AwaitedCoversInMemory()
  private val covers = CoversInMemory()
  private val cover = coverOf("image/webp", recordedBytes("covers/small.webp"))
  private val edition = onePiece()

  @Test
  fun `the fetched cover is stored and named on its edition`() {
    // Given
    awaiting(INVENTAIRE)
    val fetchAwaitedCovers = fetchAwaitedCoversAnswering()

    // When
    fetchAwaitedCovers()

    // Then
    covers.read(cover.name) shouldBe cover
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition.copy(coverName = cover.name)
  }

  private fun awaiting(chosenSource: CoverSource?) {
    editions.insert(edition)
    awaitedCovers.insert(AwaitedCover(isbnOf(ONE_PIECE), chosenSource))
  }

  private fun fetchAwaitedCoversAnswering(): FetchAwaitedCovers =
    FetchAwaitedCovers(awaitedCovers, CoverFetchAnswering(cover), covers, editions, withoutTransaction())

  private fun onePiece(): Edition =
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
    )
}
