package fr.amory.libris.bibliography.application.cover

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.AwaitedCoversInMemory
import fr.amory.libris.bibliography.fixture.CoverFetchAnswering
import fr.amory.libris.bibliography.fixture.CoversInMemory
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.coverOf
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

private const val ONE_PIECE = "9782723488525"
private const val ONE_PIECE_2 = "9782723489898"

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
    val fetchAwaitedCovers = fetchAwaitedCoversOver()

    // When
    fetchAwaitedCovers()

    // Then
    covers.read(cover.name) shouldBe cover
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition.copy(coverName = cover.name)
  }

  @Test
  fun `a stored cover is awaited no more`() {
    // Given
    awaiting(INVENTAIRE)
    val fetchAwaitedCovers = fetchAwaitedCoversOver()

    // When
    fetchAwaitedCovers()

    // Then
    awaitedCovers.findAll().shouldBeEmpty()
  }

  @Test
  fun `the name and the end of the wait land in one transaction, after the store's write`() {
    // Given
    awaiting(INVENTAIRE)
    val transactions = TransactionsObserving {
      Triple(covers.stored.size, editions.findByIsbn(isbnOf(ONE_PIECE))?.coverName, awaitedCovers.findAll().size)
    }
    val fetchAwaitedCovers =
      FetchAwaitedCovers(awaitedCovers, CoverFetchAnswering(cover), covers, editions, transactions)

    // When
    fetchAwaitedCovers()

    // Then
    transactions.recorded shouldBe listOf(
      Transaction(before = Triple(1, null, 1), after = Triple(1, cover.name, 0)),
    )
  }

  @Test
  fun `an awaited cover of another source is passed by`() {
    // Given
    awaiting(OPEN_LIBRARY)
    val coverFetch = CoverFetchAnswering(cover)
    val fetchAwaitedCovers = fetchAwaitedCoversOver(coverFetch)

    // When
    fetchAwaitedCovers()

    // Then
    coverFetch.asked.shouldBeEmpty()
    awaitedCovers.findAll() shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), OPEN_LIBRARY))
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition
  }

  @Test
  fun `an awaited cover with no chosen source is passed by`() {
    // Given
    awaiting(null)
    val coverFetch = CoverFetchAnswering(cover)
    val fetchAwaitedCovers = fetchAwaitedCoversOver(coverFetch)

    // When
    fetchAwaitedCovers()

    // Then
    coverFetch.asked.shouldBeEmpty()
    awaitedCovers.findAll() shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), null))
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition
  }

  @Test
  fun `a fetch that brings no picture keeps the wait`() {
    // Given
    awaiting(INVENTAIRE)
    val fetchAwaitedCovers = fetchAwaitedCoversOver(CoverFetchAnswering(null))

    // When
    fetchAwaitedCovers()

    // Then
    awaitedCovers.findAll() shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition
    covers.stored.shouldBeEmpty()
  }

  @Test
  fun `each awaited inventaire io cover is taken`() {
    // Given
    val second = onePiece(ONE_PIECE_2)
    awaiting(INVENTAIRE)
    awaiting(INVENTAIRE, second)
    val fetchAwaitedCovers = fetchAwaitedCoversOver()

    // When
    fetchAwaitedCovers()

    // Then
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition.copy(coverName = cover.name)
    editions.findByIsbn(isbnOf(ONE_PIECE_2)) shouldBe second.copy(coverName = cover.name)
    awaitedCovers.findAll().shouldBeEmpty()
  }

  private fun awaiting(chosenSource: CoverSource?, awaited: Edition = edition) {
    editions.insert(awaited)
    awaitedCovers.insert(AwaitedCover(checkNotNull(awaited.isbn), chosenSource))
  }

  private fun fetchAwaitedCoversOver(coverFetch: CoverFetch = CoverFetchAnswering(cover)): FetchAwaitedCovers =
    FetchAwaitedCovers(awaitedCovers, coverFetch, covers, editions, withoutTransaction())

  private fun onePiece(isbn: String = ONE_PIECE): Edition =
    Edition(
      id = EditionId.new(),
      isbn = isbnOf(isbn),
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
