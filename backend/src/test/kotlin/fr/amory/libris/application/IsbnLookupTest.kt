package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.lookup.Source.BNF
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.fixture.SourceAnswering
import fr.amory.libris.fixture.isbn13Of
import fr.amory.libris.fixture.sourceEdition
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IsbnLookupTest {
    @Test
    fun `the sources that know answer what they know together, in the order they were asked`() {
        // Given
        val lookup = IsbnLookup(
            listOf(
                SourceAnswering(BNF, Known(sourceEdition(title = "Romance dawn"))),
                SourceAnswering(OPEN_LIBRARY, Known(sourceEdition(title = "Tome 01", pageCount = 207))),
            ),
        )

        // When
        val result = lookup.lookUp(isbn13Of("9782723488525"))

        // Then
        result shouldBe Found(
            sourceEdition(
                title = "Romance dawn",
                pageCount = 207,
                coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
            ),
            listOf(BNF, OPEN_LIBRARY),
        )
    }

    @Test
    fun `a source that failed takes no part in the answer`() {
        // Given
        val lookup = IsbnLookup(
            listOf(
                SourceAnswering(BNF, Known(sourceEdition(title = "Romance dawn"))),
                SourceAnswering(OPEN_LIBRARY, Failed),
            ),
        )

        // When
        val result = lookup.lookUp(isbn13Of("9782723488525"))

        // Then
        result shouldBe Found(
            sourceEdition(
                title = "Romance dawn",
                coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
            ),
            listOf(BNF),
        )
    }

    @Test
    fun `a source that knows nothing answers that no source knows the ISBN`() {
        // Given
        val lookup = IsbnLookup(listOf(SourceAnswering(OPEN_LIBRARY, NothingKnown)))

        // When
        val result = lookup.lookUp(isbn13Of("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `a source that failed answers that no source replied`() {
        // Given
        val lookup = IsbnLookup(listOf(SourceAnswering(OPEN_LIBRARY, Failed)))

        // When
        val result = lookup.lookUp(isbn13Of("9782723488525"))

        // Then
        result shouldBe SourcesUnavailable
    }
}
