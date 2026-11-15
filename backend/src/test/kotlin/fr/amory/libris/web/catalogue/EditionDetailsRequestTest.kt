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
import io.kotest.matchers.types.shouldBeInstanceOf
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

  @Test
  fun `a language that is not two lower-case letters is refused`() {
    // Given
    val request = baggyAndHisMen().copy(language = "fre")

    // When / Then
    request.validate().shouldBeInstanceOf<Refused>().errors.map { it.field } shouldBe listOf("language")
  }

  @Test
  fun `a language in capitals is refused`() {
    // Given
    val request = baggyAndHisMen().copy(language = "FR")

    // When / Then
    request.validate().shouldBeInstanceOf<Refused>().errors.map { it.field } shouldBe listOf("language")
  }

  @Test
  fun `a publication year below one is refused`() {
    // Given
    val request = baggyAndHisMen().copy(publicationYear = 0)

    // When / Then
    request.validate().shouldBeInstanceOf<Refused>().errors.map { it.field } shouldBe listOf("publicationYear")
  }

  @Test
  fun `a page count below one is refused`() {
    // Given
    val request = baggyAndHisMen().copy(pageCount = 0)

    // When / Then
    request.validate().shouldBeInstanceOf<Refused>().errors.map { it.field } shouldBe listOf("pageCount")
  }

  @Test
  fun `a volume number below one is refused`() {
    // Given
    val request = baggyAndHisMen(volumeNumber = 0)

    // When / Then
    request.validate().shouldBeInstanceOf<Refused>().errors.map { it.field } shouldBe listOf("series")
  }

  @Test
  fun `a body without year, language, page count or volume number is accepted`() {
    // Given
    val request = baggyAndHisMen(volumeNumber = null).copy(publicationYear = null, language = null, pageCount = null)

    // When / Then
    request.validate() shouldBe Accepted(
      DETAILS_OF_BAGGY_AND_HIS_MEN.copy(
        series = SeriesEntry("One Piece", null),
        publicationYear = null,
        language = null,
        pageCount = null,
      ),
    )
  }

  @Test
  fun `a year, a page count and a volume number of one are accepted`() {
    // Given
    val request = baggyAndHisMen(volumeNumber = 1).copy(publicationYear = 1, pageCount = 1)

    // When / Then
    request.validate() shouldBe Accepted(
      DETAILS_OF_BAGGY_AND_HIS_MEN.copy(series = SeriesEntry("One Piece", 1), publicationYear = 1, pageCount = 1),
    )
  }

  private fun baggyAndHisMen(
    title: String = "Aux prises avec Baggy et ses hommes",
    artist: String = "Eiichirō Oda",
    series: String = "One Piece",
    volumeNumber: Int? = 2) =
    EditionDetailsRequest(
      kind = MANGA,
      title = title,
      subtitle = "Le clown pirate",
      authors = listOf(NewAuthorRequest("Eiichirō Oda", WRITER), NewAuthorRequest(artist, ARTIST)),
      series = NewSeriesRequest(series, volumeNumber),
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = "Luffy et Zoro affrontent le pirate Baggy le clown.",
    )
}

private val DETAILS_OF_BAGGY_AND_HIS_MEN = EditionDetails(
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
)
