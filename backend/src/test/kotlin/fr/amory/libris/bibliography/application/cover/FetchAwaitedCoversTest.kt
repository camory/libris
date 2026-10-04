package fr.amory.libris.bibliography.application.cover

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource
import fr.amory.libris.bibliography.domain.cover.CoverSource.BNF
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
import fr.amory.libris.bibliography.fixture.pictureOf
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
  fun `the fetched cover is stored normalised and named on its edition`() {
    // Given
    awaiting(OPEN_LIBRARY)
    val tall = coverOf("image/jpeg", recordedBytes("covers/tall.jpg"))
    val fetchAwaitedCovers = fetchAwaitedCoversOver(listOf(CoverFetchAnswering(OPEN_LIBRARY, tall)))

    // When
    fetchAwaitedCovers()

    // Then
    covers.stored.single().name shouldBe tall.normalised().name
    editions.findByIsbn(isbnOf(ONE_PIECE))?.coverName shouldBe tall.normalised().name
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
      FetchAwaitedCovers(awaitedCovers, listOf(CoverFetchAnswering(INVENTAIRE, cover)), covers, editions, transactions)

    // When
    fetchAwaitedCovers()

    // Then
    transactions.recorded shouldBe listOf(
      Transaction(before = Triple(1, null, 1), after = Triple(1, cover.name, 0)),
    )
  }

  @Test
  fun `an awaited Open Library cover is fetched from Open Library alone`() {
    // Given
    awaiting(OPEN_LIBRARY)
    val jpeg = coverOf("image/jpeg", pictureOf("jpeg", 400, 600))
    val inventaire = CoverFetchAnswering(INVENTAIRE, cover)
    val fetchAwaitedCovers = fetchAwaitedCoversOver(listOf(inventaire, CoverFetchAnswering(OPEN_LIBRARY, jpeg)))

    // When
    fetchAwaitedCovers()

    // Then
    covers.read(jpeg.name) shouldBe jpeg
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition.copy(coverName = jpeg.name)
    awaitedCovers.findAll().shouldBeEmpty()
    inventaire.asked.shouldBeEmpty()
  }

  @Test
  fun `an awaited cover of a source no fetch serves is passed by`() {
    // Given
    awaiting(BNF)
    val inventaire = CoverFetchAnswering(INVENTAIRE, cover)
    val openLibrary = CoverFetchAnswering(OPEN_LIBRARY, coverOf("image/jpeg", recordedBytes("covers/tall.jpg")))
    val fetchAwaitedCovers = fetchAwaitedCoversOver(listOf(inventaire, openLibrary))

    // When
    fetchAwaitedCovers()

    // Then
    inventaire.asked.shouldBeEmpty()
    openLibrary.asked.shouldBeEmpty()
    awaitedCovers.findAll() shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), BNF))
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition
  }

  @Test
  fun `with no chosen source, the first source in order that has a picture is fetched`() {
    // Given
    awaiting(null)
    val openLibrary = CoverFetchAnswering(OPEN_LIBRARY, coverOf("image/jpeg", recordedBytes("covers/tall.jpg")))
    val fetchAwaitedCovers = fetchAwaitedCoversOver(listOf(openLibrary, CoverFetchAnswering(INVENTAIRE, cover)))

    // When
    fetchAwaitedCovers()

    // Then
    covers.read(cover.name) shouldBe cover
    editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition.copy(coverName = cover.name)
    awaitedCovers.findAll().shouldBeEmpty()
    openLibrary.asked.shouldBeEmpty()
  }

  @Test
  fun `the cascade goes on past a source with no picture`() {
    // Given
    awaiting(null)
    val tall = coverOf("image/jpeg", recordedBytes("covers/tall.jpg"))
    val inventaire = CoverFetchAnswering(INVENTAIRE, null)
    val openLibrary = CoverFetchAnswering(OPEN_LIBRARY, null)
    val bnf = CoverFetchAnswering(BNF, tall)
    val fetchAwaitedCovers = fetchAwaitedCoversOver(listOf(bnf, openLibrary, inventaire))

    // When
    fetchAwaitedCovers()

    // Then
    covers.stored.single().name shouldBe tall.normalised().name
    editions.findByIsbn(isbnOf(ONE_PIECE))?.coverName shouldBe tall.normalised().name
    inventaire.asked shouldBe listOf(isbnOf(ONE_PIECE))
    openLibrary.asked shouldBe listOf(isbnOf(ONE_PIECE))
    bnf.asked shouldBe listOf(isbnOf(ONE_PIECE))
  }

  @Test
  fun `a fetch that brings no picture keeps the wait`() {
    // Given
    awaiting(INVENTAIRE)
    val fetchAwaitedCovers = fetchAwaitedCoversOver(listOf(CoverFetchAnswering(INVENTAIRE, null)))

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

  private fun fetchAwaitedCoversOver(
    coverFetches: List<CoverFetch> = listOf(CoverFetchAnswering(INVENTAIRE, cover))): FetchAwaitedCovers =
    FetchAwaitedCovers(awaitedCovers, coverFetches, covers, editions, withoutTransaction())

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
