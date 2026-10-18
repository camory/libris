package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HoldEditionTest {
  private val editions = EditionsInMemory()
  private val holdEdition = HoldEdition(editions)

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
      series = SeriesEntry("One piece", 1),
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
