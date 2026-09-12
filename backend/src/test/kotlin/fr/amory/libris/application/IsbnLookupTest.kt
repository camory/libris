package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.fixture.SourceAnswering
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private fun isbn13Of(text: String): Isbn13 = checkNotNull(Isbn13.of(text))

private val ONE_PIECE_1 = SourceEdition(
    isbn13 = isbn13Of("9782723488525"),
    title = "Romance dawn",
    subtitle = "à l'aube d'une grande aventure",
    authors = listOf(SourceAuthor("Eiichirō Oda", WRITER)),
    series = null,
    collection = null,
    publisher = "Glénat",
    publicationYear = 2013,
    language = null,
    pageCount = 203,
    summary = null,
    coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
)

class IsbnLookupTest {
    @Test
    fun `a source that knows the ISBN answers what it knows and names itself`() {
        // Given
        val lookup = IsbnLookup(SourceAnswering(OPEN_LIBRARY, Known(ONE_PIECE_1)))

        // When
        val result = lookup.lookUp(isbn13Of("9782723488525"))

        // Then
        result shouldBe Found(ONE_PIECE_1, listOf(OPEN_LIBRARY))
    }

    @Test
    fun `a source that knows nothing answers that no source knows the ISBN`() {
        // Given
        val lookup = IsbnLookup(SourceAnswering(OPEN_LIBRARY, NothingKnown))

        // When
        val result = lookup.lookUp(isbn13Of("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `a source that failed answers that no source replied`() {
        // Given
        val lookup = IsbnLookup(SourceAnswering(OPEN_LIBRARY, Failed))

        // When
        val result = lookup.lookUp(isbn13Of("9782723488525"))

        // Then
        result shouldBe SourcesUnavailable
    }
}
