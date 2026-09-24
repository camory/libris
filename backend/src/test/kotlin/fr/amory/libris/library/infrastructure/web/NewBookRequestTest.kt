package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.library.infrastructure.web.NewBookValidation.Refused
import io.kotest.matchers.shouldBe
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

    private fun romanceDawn(
        isbn13: String = "9782723488525",
        title: String = "Romance dawn",
        author: String = "Eiichirō Oda",
        series: String = "One Piece",
    ) = NewBookRequest(
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
        coverUrl = null,
    )
}
