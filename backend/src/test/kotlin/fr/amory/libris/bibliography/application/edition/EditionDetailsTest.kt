package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditionDetailsTest {
  @Test
  fun `the details of an edition without a title are none`() {
    // When
    val details = EditionDetails.of(
      kind = MANGA,
      title = " ",
      subtitle = null,
      contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
      series = SeriesEntry("One Piece", 2),
      collection = null,
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = null,
    )

    // Then
    details shouldBe null
  }

  @Test
  fun `the details of an edition without a title are refused`() {
    shouldThrow<IllegalArgumentException> {
      EditionDetails(
        kind = MANGA,
        title = " ",
        subtitle = null,
        contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
        series = SeriesEntry("One Piece", 2),
        collection = null,
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        pageCount = 208,
        summary = null,
      )
    }
  }
}
