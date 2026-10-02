package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Accepted
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Refused
import fr.amory.libris.shared.infrastructure.web.ValidationErrorResponse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class NewBookRequestTest {
  @Test
  fun `a blank title is refused`() {
    val request = romanceDawn(title = " ")

    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("title", "blank")))
  }

  @Test
  fun `the thirteen digits with separators are not an isbn13`() {
    val request = romanceDawn(isbn13 = "978-2-7234-8852-5")

    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("isbn13", "not-an-isbn")))
  }

  @Test
  fun `an author without a name is refused`() {
    val request = romanceDawn(author = " ")

    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("authors", "blank")))
  }

  @Test
  fun `a series without a name is refused`() {
    val request = romanceDawn(series = " ")

    request.validate() shouldBe Refused(listOf(ValidationErrorResponse("series", "blank")))
  }

  @Test
  fun `every refused field has its error`() {
    val request = romanceDawn(isbn13 = "9782723488526", title = " ")

    request.validate() shouldBe Refused(
      listOf(ValidationErrorResponse("isbn13", "not-an-isbn"), ValidationErrorResponse("title", "blank")),
    )
  }

  @Test
  fun `the name of a source becomes the book's source`() {
    // Given
    val request = romanceDawn(coverSource = "inventaire.io")

    // When / Then
    request.validate().shouldBeInstanceOf<Accepted>().book.coverSource shouldBe INVENTAIRE
  }

  @Test
  fun `a name no source bears is accepted as no source`() {
    // Given
    val request = romanceDawn(coverSource = "Libris")

    // When / Then
    request.validate().shouldBeInstanceOf<Accepted>().book.coverSource shouldBe null
  }

  private fun romanceDawn(
    isbn13: String = "9782723488525",
    title: String = "Romance dawn",
    author: String = "Eiichirō Oda",
    series: String = "One Piece",
    coverSource: String? = null) =
    NewBookRequest(
      isbn13 = isbn13,
      kind = MANGA,
      title = title,
      subtitle = null,
      authors = listOf(NewAuthorRequest(author, WRITER)),
      series = NewSeriesRequest(series, 1),
      collection = null,
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 207,
      summary = null,
      coverSource = coverSource,
    )
}
