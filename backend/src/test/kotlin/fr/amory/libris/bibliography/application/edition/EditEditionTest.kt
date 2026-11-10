package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditEditionResult.NoSuchEdition
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

class EditEditionTest {
  private val editions = EditionsInMemory()
  private val seriesRepository = SeriesInMemory()
  private val editEdition = EditEdition(editions, seriesRepository, withoutTransaction())

  @Test
  fun `an edition the house lacks is no such edition`() {
    // Given
    editions.insert(asterixLeGaulois())

    // When
    val result = editEdition(EditionId.new(), asEdited())

    // Then
    result shouldBe NoSuchEdition
    editions.stored shouldBe listOf(asterixLeGaulois())
  }

  @Test
  fun `the held edition is written with the details sent, its id, ISBN and cover name kept`() {
    // Given
    editions.insert(asterixLeGaulois())

    // When
    val result = editEdition(ASTERIX_LE_GAULOIS, asEdited())

    // Then
    val edited = Edition(
      id = ASTERIX_LE_GAULOIS,
      isbn = isbnOf("9782012101333"),
      kind = BD,
      title = "Astérix le Gaulois",
      subtitle = "Une aventure d'Astérix",
      contributions = Contributions.of(
        listOf(Contribution("René Goscinny", WRITER), Contribution("Albert Uderzo", ARTIST)),
      ),
      seriesVolume = null,
      collection = "Les aventures d'Astérix",
      publisher = "Dargaud",
      publicationYear = 1961,
      language = "fr",
      pageCount = 48,
      summary = "Nous sommes en 50 avant Jésus-Christ.",
      coverName = ASTERIX_COVER,
    )
    result shouldBe Edited(edited)
    editions.stored shouldBe listOf(edited)
  }

  @Test
  fun `a series name the house holds, in another capitalisation, reaches the stored series`() {
    // Given
    editions.insert(asterixLeGaulois())
    val stored = seriesRepository.findOrInsert(Series(SeriesId.new(), "Astérix"))

    // When
    editEdition(ASTERIX_LE_GAULOIS, asEdited().copy(series = SeriesEntry("ASTÉRIX", 2)))

    // Then
    editions.stored.single().seriesVolume shouldBe SeriesVolume(stored.id, 2)
    seriesRepository.stored shouldBe listOf(stored)
  }

  @Test
  fun `the series and the edition are written in one transaction`() {
    // Given
    editions.insert(asterixLeGaulois())
    val transactions = TransactionsObserving {
      Pair(seriesRepository.stored.map { it.name }, editions.stored.map { it.title })
    }
    val editEdition = EditEdition(editions, seriesRepository, transactions)

    // When
    editEdition(ASTERIX_LE_GAULOIS, asEdited().copy(series = SeriesEntry("Astérix", 1)))

    // Then
    transactions.recorded shouldBe listOf(
      Transaction(
        before = Pair(emptyList(), listOf("Asterix le gaulois")),
        after = Pair(listOf("Astérix"), listOf("Astérix le Gaulois")),
      ),
    )
  }

  @Test
  fun `the edition answered is the one the house holds after the write`() {
    // Given
    val editions = EditionsRespellingAuthors(mapOf("rene goscinny" to "René Goscinny"))
    editions.insert(asterixLeGaulois())
    val editEdition = EditEdition(editions, seriesRepository, withoutTransaction())

    // When
    val result = editEdition(
      ASTERIX_LE_GAULOIS,
      asEdited().copy(contributions = Contributions.of(listOf(Contribution("rene goscinny", WRITER)))),
    )

    // Then
    result.shouldBeInstanceOf<Edited>().edition.contributions.toList() shouldBe
      listOf(Contribution("René Goscinny", WRITER))
  }

  private fun asterixLeGaulois(): Edition =
    Edition(
      id = ASTERIX_LE_GAULOIS,
      isbn = isbnOf("9782012101333"),
      kind = BOOK,
      title = "Asterix le gaulois",
      subtitle = null,
      contributions = Contributions.of(listOf(Contribution("René Goscinny", WRITER))),
      seriesVolume = SeriesVolume(ASTERIX, 1),
      collection = null,
      publisher = "Hachette",
      publicationYear = 1999,
      language = null,
      pageCount = null,
      summary = null,
      coverName = ASTERIX_COVER,
    )

  private fun asEdited(): EditionDetails =
    EditionDetails(
      kind = BD,
      title = "Astérix le Gaulois",
      subtitle = "Une aventure d'Astérix",
      contributions = Contributions.of(
        listOf(Contribution("René Goscinny", WRITER), Contribution("Albert Uderzo", ARTIST)),
      ),
      series = null,
      collection = "Les aventures d'Astérix",
      publisher = "Dargaud",
      publicationYear = 1961,
      language = "fr",
      pageCount = 48,
      summary = "Nous sommes en 50 avant Jésus-Christ.",
    )
}

private class EditionsRespellingAuthors(
  private val houseSpellings: Map<String, String>,
  private val editions: EditionsInMemory = EditionsInMemory()) : EditionRepository by editions {
  override fun update(edition: Edition) =
    editions.update(edition.copy(contributions = Contributions.of(edition.contributions.map { respelled(it) })))

  private fun respelled(contribution: Contribution): Contribution =
    contribution.copy(name = houseSpellings[contribution.name] ?: contribution.name)
}

private val ASTERIX_LE_GAULOIS = EditionId.new()
private val ASTERIX = SeriesId.new()
private val ASTERIX_COVER = CoverName("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
