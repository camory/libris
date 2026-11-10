package fr.amory.libris.web.catalogue

import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.web.bookshelf.NewAuthorRequest
import fr.amory.libris.web.bookshelf.NewSeriesRequest
import fr.amory.libris.web.catalogue.EditionDetailsValidation.Accepted
import fr.amory.libris.web.catalogue.EditionDetailsValidation.Refused
import fr.amory.libris.web.problem.ValidationErrorResponse
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditionDetailsRequestTest {
  @Test
  fun `a body of every field is accepted as the details of an edition`() {
    // Given
    val request = baggyAndHisMen()

    // When / Then
    request.validate() shouldBe Accepted(
      EditionDetails(
        kind = MANGA,
        title = "Aux prises avec Baggy et ses hommes",
        subtitle = "Le clown pirate",
        contributions = Contributions.of(
          listOf(Contribution("Eiichirō Oda", WRITER), Contribution("Eiichirō Oda", ARTIST)),
        ),
        series = SeriesEntry("One Piece", 2),
        collection = "Shonen manga",
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        pageCount = 208,
        summary = "Luffy et Zoro affrontent le pirate Baggy le clown.",
      ),
    )
  }

  @Test
  fun `a blank title is refused`() {
    // Given
    val request = baggyAndHisMen(title = " ")

    // When / Then
    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("title", "blank")))
  }

  @Test
  fun `an author without a name is refused`() {
    // Given
    val request = baggyAndHisMen(artist = " ")

    // When / Then
    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("authors", "blank")))
  }

  @Test
  fun `a series without a name is refused`() {
    // Given
    val request = baggyAndHisMen(series = " ")

    // When / Then
    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("series", "blank")))
  }

  private fun baggyAndHisMen(
    title: String = "Aux prises avec Baggy et ses hommes",
    artist: String = "Eiichirō Oda",
    series: String = "One Piece") =
    EditionDetailsRequest(
      kind = MANGA,
      title = title,
      subtitle = "Le clown pirate",
      authors = listOf(NewAuthorRequest("Eiichirō Oda", WRITER), NewAuthorRequest(artist, ARTIST)),
      series = NewSeriesRequest(series, 2),
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = "Luffy et Zoro affrontent le pirate Baggy le clown.",
    )
}
