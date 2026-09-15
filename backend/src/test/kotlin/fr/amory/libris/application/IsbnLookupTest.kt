package fr.amory.libris.application

import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.fixture.A_SOURCE_EDITION
import fr.amory.libris.fixture.SourceAnswering
import fr.amory.libris.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IsbnLookupTest {
    @Test
    fun `the source that knows answers what it knows`() {
        // Given
        val edition = A_SOURCE_EDITION.copy(title = "Romance dawn")
        val lookup = IsbnLookup(SourceAnswering(Known(edition)))

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(edition)
    }

    @Test
    fun `a source knowing nothing answers that the ISBN is unknown`() {
        // Given
        val lookup = IsbnLookup(SourceAnswering(NothingKnown))

        // When
        val result = lookup.lookUp(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `a source having failed answers that no source replied`() {
        // Given
        val lookup = IsbnLookup(SourceAnswering(Failed))

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe SourcesUnavailable
    }
}
