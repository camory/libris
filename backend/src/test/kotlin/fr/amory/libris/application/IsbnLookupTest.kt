package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.fixture.SourceAnswering
import fr.amory.libris.fixture.isbn13Of
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private val AN_EDITION = SourceEdition(
    isbn13 = isbn13Of("9782723488525"),
    title = "Un ouvrage",
    subtitle = null,
    authors = emptyList(),
    series = null,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
)

class IsbnLookupTest {
    @Test
    fun `a source that knows the ISBN answers what it knows and names itself`() {
        // Given
        val lookup = IsbnLookup(SourceAnswering(OPEN_LIBRARY, Known(AN_EDITION)))

        // When
        val result = lookup.lookUp(isbn13Of("9782723488525"))

        // Then
        result shouldBe Found(AN_EDITION, listOf(OPEN_LIBRARY))
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
