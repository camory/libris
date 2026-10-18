package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class NewEditionTest {
  @Test
  fun `a new edition without a title is none`() {
    val newEdition = NewEdition.of(
      isbn = isbnOf("9782723488525"),
      kind = MANGA,
      title = " ",
      subtitle = null,
      contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
      series = SeriesEntry("One Piece", 1),
      collection = null,
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 207,
      summary = null,
      coverSource = null,
    )

    newEdition shouldBe null
  }

  @Test
  fun `a new edition without a title is refused`() {
    shouldThrow<IllegalArgumentException> {
      NewEdition(
        isbn = isbnOf("9782723488525"),
        kind = MANGA,
        title = " ",
        subtitle = null,
        contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
        series = SeriesEntry("One Piece", 1),
        collection = null,
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        pageCount = 207,
        summary = null,
        coverSource = null,
      )
    }
  }
}
