package fr.amory.libris.bibliography.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.domain.lookup.Source.BNF
import fr.amory.libris.bibliography.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.LookupAnswering
import fr.amory.libris.bibliography.fixture.LookupAnsweringAtRendezvous
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier

private val ROMANCE_DAWN = Edition(
    id = EditionId.new(),
    isbn = isbnOf("9782723488525"),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = "Tome 01",
    contributions = Contributions.of(emptyList()),
    series = null,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
)

class LookupEditionByIsbnTest {
    @Test
    fun `an ISBN the house holds is answered as the house holds it, without asking the sources`() {
        // Given
        val source = LookupAnswering(Known(A_PREVIEW.copy(title = "Un titre venu d'une source")))
        val lookup = LookupEditionByIsbn(EditionsInMemory().also { it.insert(ROMANCE_DAWN) }, listOf(source))

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Held(ROMANCE_DAWN)
        source.asked shouldBe emptyList()
    }

    @Test
    fun `the BnF takes precedence over Open Library, whatever the order they were given`() {
        // Given
        val lookup = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnswering(Known(A_PREVIEW.copy(title = "Tome 01", pageCount = 207)), OPEN_LIBRARY),
                LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn")), BNF),
            ),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn", pageCount = 207))
    }

    @Test
    fun `a source that failed takes no part in the answer`() {
        // Given
        val lookup = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnswering(Failed),
                LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn"))),
            ),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn"))
    }

    @Test
    fun `no source knowing the ISBN answers that it is unknown`() {
        // Given
        val lookup = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(LookupAnswering(NothingKnown), LookupAnswering(NothingKnown)),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `every source having failed answers that no source replied`() {
        // Given
        val lookup = LookupEditionByIsbn(EditionsInMemory(), listOf(LookupAnswering(Failed), LookupAnswering(Failed)))

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe SourcesUnavailable
    }

    @Test
    fun `one source failing while the other knows nothing answers that the ISBN is unknown`() {
        // Given
        val lookup = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(LookupAnswering(Failed), LookupAnswering(NothingKnown)),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `every source is asked at once`() {
        // Given
        val rendezvous = CyclicBarrier(2)
        val lookup = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnsweringAtRendezvous(rendezvous, Known(A_PREVIEW.copy(title = "Romance dawn"))),
                LookupAnsweringAtRendezvous(rendezvous, Known(A_PREVIEW.copy(pageCount = 207))),
            ),
        )

        // When
        val result = lookup.lookUp(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn", pageCount = 207))
    }
}
