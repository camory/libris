package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import fr.amory.libris.bibliography.fixture.LookupAnswering
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.library.application.IsbnLookupResult.Found
import fr.amory.libris.library.application.IsbnLookupResult.UnknownIsbn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private const val ONE_PIECE = "9782723488525"

class LookupIsbnForReaderTest {
    @Test
    fun `an ISBN the house lacks is answered by the sources, with no copy`() {
        // Given
        val lookup = lookupAsking(LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn"))))

        // When
        val result = lookup.lookUp(isbnOf(ONE_PIECE))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn"), emptyList())
    }

    @Test
    fun `an ISBN no source knows is unknown`() {
        // Given
        val lookup = lookupAsking(LookupAnswering(NothingKnown))

        // When
        val result = lookup.lookUp(isbnOf(ONE_PIECE))

        // Then
        result shouldBe UnknownIsbn
    }

    private fun lookupAsking(vararg sources: ExternalEditionLookup): LookupIsbnForReader =
        LookupIsbnForReader(LookupEditionByIsbn(sources.toList()))
}
