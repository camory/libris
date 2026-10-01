package fr.amory.libris.bibliography.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.CoverCandidates
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.domain.lookup.Source.BNF
import fr.amory.libris.bibliography.domain.lookup.Source.INVENTAIRE
import fr.amory.libris.bibliography.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import fr.amory.libris.bibliography.fixture.CoverLookupAnswering
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.LookupAnswering
import fr.amory.libris.bibliography.fixture.LookupAnsweringAtRendezvous
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

private val NO_COVER = CoverCandidates.of(emptyList())

class LookupEditionByIsbnTest {
    @Test
    fun `an ISBN the house holds is answered as the house holds it, without asking the sources`() {
        // Given
        val source = LookupAnswering(Known(A_PREVIEW.copy(title = "Un titre venu d'une source")))
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory().also { it.insert(ROMANCE_DAWN) },
            listOf(source),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result shouldBe Held(ROMANCE_DAWN.id, checkNotNull(EditionPreview.of(ROMANCE_DAWN)))
        source.asked shouldBe emptyList()
    }

    @Test
    fun `the BnF takes precedence over Open Library, whatever the order they were given`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnswering(Known(A_PREVIEW.copy(title = "Tome 01", pageCount = 207)), OPEN_LIBRARY),
                LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn")), BNF),
            ),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn", pageCount = 207), NO_COVER)
    }

    @Test
    fun `a source that failed takes no part in the answer`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnswering(Failed),
                LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn"))),
            ),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn"), NO_COVER)
    }

    @Test
    fun `no source knowing the ISBN answers that it is unknown`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(LookupAnswering(NothingKnown), LookupAnswering(NothingKnown)),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `every source having failed answers that no source replied`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(LookupAnswering(Failed), LookupAnswering(Failed)),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result shouldBe SourcesUnavailable
    }

    @Test
    fun `one source failing while the other knows nothing answers that the ISBN is unknown`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(LookupAnswering(Failed), LookupAnswering(NothingKnown)),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782000000013"))

        // Then
        result shouldBe UnknownIsbn
    }

    @Test
    fun `every source is asked at once`() {
        // Given
        val rendezvous = CyclicBarrier(2)
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnsweringAtRendezvous(rendezvous, Known(A_PREVIEW.copy(title = "Romance dawn"))),
                LookupAnsweringAtRendezvous(rendezvous, Known(A_PREVIEW.copy(pageCount = 207))),
            ),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result shouldBe Found(A_PREVIEW.copy(title = "Romance dawn", pageCount = 207), NO_COVER)
    }

    @Test
    fun `the edition sources offer their covers in the house's order, whatever the order they were given`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnswering(Known(A_PREVIEW.copy(coverUrl = "https://bnf/b")), BNF),
                LookupAnswering(Known(A_PREVIEW.copy(coverUrl = "https://openlibrary/a")), OPEN_LIBRARY),
            ),
            NO_COVER_LOOKUP,
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result.shouldBeInstanceOf<Found>().covers.toList() shouldBe listOf(
            CoverCandidate(OPEN_LIBRARY, "https://openlibrary/a"),
            CoverCandidate(BNF, "https://bnf/b"),
        )
    }

    @Test
    fun `inventaire io's picture comes before the edition sources' covers`() {
        // Given
        val lookupEditionByIsbn = LookupEditionByIsbn(
            EditionsInMemory(),
            listOf(
                LookupAnswering(Known(A_PREVIEW.copy(coverUrl = "https://bnf/b")), BNF),
                LookupAnswering(Known(A_PREVIEW.copy(coverUrl = "https://openlibrary/a")), OPEN_LIBRARY),
            ),
            CoverLookupAnswering(CoverCandidate(INVENTAIRE, "https://inventaire/c")),
        )

        // When
        val result = lookupEditionByIsbn(isbnOf("9782723488525"))

        // Then
        result.shouldBeInstanceOf<Found>().covers.toList() shouldBe listOf(
            CoverCandidate(INVENTAIRE, "https://inventaire/c"),
            CoverCandidate(OPEN_LIBRARY, "https://openlibrary/a"),
            CoverCandidate(BNF, "https://bnf/b"),
        )
    }

    private companion object {
        val NO_COVER_LOOKUP get() = CoverLookupAnswering(null)
    }
}
