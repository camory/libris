package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class NewBookTest {
    @Test
    fun `a new book without a title is none`() {
        val book = NewBook.of(
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
            coverUrl = null,
        )

        book shouldBe null
    }
}
