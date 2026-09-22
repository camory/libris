package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.LookupAnswering
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.library.application.IsbnLookupResult.Found
import fr.amory.libris.library.application.IsbnLookupResult.SourcesUnavailable
import fr.amory.libris.library.application.IsbnLookupResult.UnknownIsbn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private const val ONE_PIECE = "9782723488525"

private val ROMANCE_DAWN = Edition(
    id = EditionId.new(),
    isbn = isbnOf(ONE_PIECE),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = "Tome 01",
    contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
    series = SeriesEntry("One Piece", 1),
    collection = "Shōnen",
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 207,
    summary = "Luffy rêve de devenir le roi des pirates.",
    coverUrl = "https://couvertures.amory.fr/one-piece-01.jpg",
)

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

    @Test
    fun `an ISBN whose sources are all down answers that the sources are unavailable`() {
        // Given
        val lookup = lookupAsking(LookupAnswering(Failed))

        // When
        val result = lookup.lookUp(isbnOf(ONE_PIECE))

        // Then
        result shouldBe SourcesUnavailable
    }

    @Test
    fun `an ISBN the house holds is answered as the house holds it, without asking the sources`() {
        // Given
        val source = LookupAnswering(Known(A_PREVIEW.copy(title = "Un titre venu d'une source")))
        val lookup = lookupAsking(source, house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) })

        // When
        val result = lookup.lookUp(isbnOf(ONE_PIECE))

        // Then
        result shouldBe Found(
            EditionPreview(
                isbn = isbnOf(ONE_PIECE),
                kind = MANGA,
                title = "Romance dawn",
                subtitle = "Tome 01",
                contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
                series = SeriesEntry("One Piece", 1),
                collection = "Shōnen",
                publisher = "Glénat",
                publicationYear = 2013,
                language = "fr",
                pageCount = 207,
                summary = "Luffy rêve de devenir le roi des pirates.",
                coverUrl = "https://couvertures.amory.fr/one-piece-01.jpg",
            ),
            emptyList(),
        )
        source.asked shouldBe emptyList()
    }

    private fun lookupAsking(
        vararg sources: ExternalEditionLookup,
        house: EditionsInMemory = EditionsInMemory(),
    ): LookupIsbnForReader = LookupIsbnForReader(house, LookupEditionByIsbn(sources.toList()))
}
