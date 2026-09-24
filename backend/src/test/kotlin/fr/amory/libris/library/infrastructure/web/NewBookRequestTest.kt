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

    private fun romanceDawn(title: String = "Romance dawn") = NewBookRequest(
        isbn13 = "9782723488525",
        kind = MANGA,
        title = title,
        subtitle = null,
        authors = listOf(NewAuthorRequest("Eiichirō Oda", WRITER)),
        series = NewSeriesRequest("One Piece", 1),
        collection = null,
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        pageCount = 207,
        summary = null,
        coverUrl = null,
    )
}
