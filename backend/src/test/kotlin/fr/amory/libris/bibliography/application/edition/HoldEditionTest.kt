package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.AwaitedCoversInMemory
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

class HoldEditionTest {
  private val editions = EditionsInMemory()
  private val seriesRepository = SeriesInMemory()
  private val awaitedCovers = AwaitedCoversInMemory()
  private val holdEdition = HoldEdition(editions, seriesRepository, awaitedCovers, withoutTransaction())

  @Test
  fun `the house lacking the ISBN gains the edition`() {
    // When
    val edition = holdEdition(onePieceTomeOne())

    // Then
    edition shouldBe Edition(
      id = edition.id,
      isbn = isbnOf(ONE_PIECE),
      kind = MANGA,
      title = "Romance dawn",
      subtitle = "À l'aube d'une grande aventure",
      contributions = Contributions.of(listOf(Contribution("Eiichiro Oda", WRITER))),
      seriesVolume = SeriesVolume(seriesRepository.stored.single().id, 1),
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = "Luffy prend la mer pour devenir le roi des pirates.",
      coverName = null,
    )
    editions.stored shouldBe listOf(edition)
  }

  @Test
  fun `the edition the house gains refers to the series of its name`() {
    // When
    val edition = holdEdition(onePieceTomeOne())

    // Then
    val series = seriesRepository.stored.single()
    series shouldBe Series(series.id, "One piece")
    edition.seriesVolume shouldBe SeriesVolume(series.id, 1)
  }

  @Test
  fun `a series name the house holds, in another capitalisation, reaches the stored series`() {
    // Given
    val stored = seriesRepository.findOrInsert(Series(SeriesId.new(), "One piece"))

    // When
    val edition = holdEdition(onePieceTomeOne().copy(series = SeriesEntry("ONE PIECE", 1)))

    // Then
    edition.seriesVolume shouldBe SeriesVolume(stored.id, 1)
    seriesRepository.stored shouldBe listOf(stored)
  }

  @Test
  fun `an ISBN the house holds reaches the existing edition`() {
    // Given
    val held = holdEdition(onePieceTomeOne())

    // When
    val edition = holdEdition(onePieceTomeOne())

    // Then
    edition shouldBe held
    editions.stored shouldBe listOf(held)
  }

  @Test
  fun `an ISBN the house holds finds or inserts no series`() {
    // Given
    holdEdition(onePieceTomeOne())

    // When
    holdEdition(onePieceTomeOne().copy(series = SeriesEntry("Naruto", 1)))

    // Then
    seriesRepository.stored.map { it.name } shouldBe listOf("One piece")
  }

  @Test
  fun `the edition the house gains awaits its cover from the chosen source`() {
    // When
    holdEdition(onePieceTomeOne().copy(coverSource = INVENTAIRE))

    // Then
    awaitedCovers.stored shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
  }

  @Test
  fun `an edition gained with no chosen source awaits its cover all the same`() {
    // When
    holdEdition(onePieceTomeOne().copy(coverSource = null))

    // Then
    awaitedCovers.stored shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), null))
  }

  @Test
  fun `the ouvrage without an ISBN gives the house a new edition every time`() {
    // Given
    val withoutIsbn = onePieceTomeOne().copy(isbn = null)
    val first = holdEdition(withoutIsbn)

    // When
    val second = holdEdition(withoutIsbn)

    // Then
    second.id shouldNotBe first.id
    editions.stored shouldBe listOf(first, second)
    editions.stored.map { it.isbn } shouldBe listOf(null, null)
  }

  @Test
  fun `an ISBN the house holds awaits no second cover`() {
    // Given
    holdEdition(onePieceTomeOne().copy(coverSource = INVENTAIRE))

    // When
    holdEdition(onePieceTomeOne().copy(coverSource = OPEN_LIBRARY))

    // Then
    awaitedCovers.stored shouldBe listOf(AwaitedCover(isbnOf(ONE_PIECE), INVENTAIRE))
  }

  @Test
  fun `the edition and its awaited cover land in one transaction`() {
    // Given
    val transactions = TransactionsObserving { editions.stored.size to awaitedCovers.stored.size }
    val holdEdition = HoldEdition(editions, seriesRepository, awaitedCovers, transactions)

    // When
    holdEdition(onePieceTomeOne())

    // Then
    transactions.recorded shouldBe listOf(Transaction(before = 0 to 0, after = 1 to 1))
  }

  private fun onePieceTomeOne(): NewEdition =
    NewEdition(
      isbn = isbnOf(ONE_PIECE),
      kind = MANGA,
      title = "Romance dawn",
      subtitle = "À l'aube d'une grande aventure",
      contributions = Contributions.of(listOf(Contribution("Eiichiro Oda", WRITER))),
      series = SeriesEntry("One piece", 1),
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = "Luffy prend la mer pour devenir le roi des pirates.",
      coverSource = null,
    )
}

private const val ONE_PIECE = "9782723488525"
