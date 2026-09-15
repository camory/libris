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
    fun `the sources that know answer what they know together, in the order they were asked`() {
        // Given
        val lookup = IsbnLookup(
            listOf(
                SourceAnswering(Known(A_SOURCE_EDITION.copy(title = "Romance dawn"))),
                SourceAnswering(Known(A_SOURCE_EDITION.copy(title = "Tome 01", pageCount = 207))),
            ),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_SOURCE_EDITION.copy(title = "Romance dawn", pageCount = 207))
    }

    @Test
    fun `a source that failed takes no part in the answer`() {
        // Given
        val lookup = IsbnLookup(
            listOf(
                SourceAnswering(Failed),
                SourceAnswering(Known(A_SOURCE_EDITION.copy(title = "Romance dawn"))),
            ),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_SOURCE_EDITION.copy(title = "Romance dawn"))
    }

    @Test
    fun `no source knowing the ISBN answers that it is unknown`() {
        // Given
        val lookup = IsbnLookup(listOf(SourceAnswering(NothingKnown), SourceAnswering(NothingKnown)))

        // When
        val result = lookup.lookUp(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `every source having failed answers that no source replied`() {
        // Given
        val lookup = IsbnLookup(listOf(SourceAnswering(Failed), SourceAnswering(Failed)))

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe SourcesUnavailable
    }

    @Test
    fun `one source failing while the other knows nothing answers that the ISBN is unknown`() {
        // Given
        val lookup = IsbnLookup(listOf(SourceAnswering(Failed), SourceAnswering(NothingKnown)))

        // When
        val result = lookup.lookUp(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }
}
