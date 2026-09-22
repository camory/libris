package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.LookupAnswering
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.library.application.IsbnLookupResult.Held
import fr.amory.libris.library.application.IsbnLookupResult.NotHeld
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.ReaderCopiesInMemory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
    private val lea = ReaderId.new()

    @Test
    fun `an ISBN the house lacks is answered by the sources`() {
        // Given
        val lookup = lookupAsking(LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn"))))

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe NotHeld(EditionLookupResult.Found(A_PREVIEW.copy(title = "Romance dawn")))
    }

    @Test
    fun `an ISBN no source knows is answered unknown by the sources`() {
        // Given
        val lookup = lookupAsking(LookupAnswering(NothingKnown))

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe NotHeld(EditionLookupResult.UnknownIsbn)
    }

    @Test
    fun `an ISBN whose sources are all down is answered sources unavailable`() {
        // Given
        val lookup = lookupAsking(LookupAnswering(Failed))

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe NotHeld(EditionLookupResult.SourcesUnavailable)
    }

    @Test
    fun `an ISBN the house holds is answered as the house holds it, without asking the sources`() {
        // Given
        val source = LookupAnswering(Known(A_PREVIEW.copy(title = "Un titre venu d'une source")))
        val lookup = lookupAsking(source, house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) })

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe Held(ROMANCE_DAWN, emptyList())
        source.asked shouldBe emptyList()
    }

    @Test
    fun `the copies on the reader's bookshelves come with the edition`() {
        // Given
        val onLeasBookshelf = CopyOnBookshelf(CopyId.new(), BookshelfId.new(), "Bibliothèque de Léa")
        val inTheSalon = CopyOnBookshelf(CopyId.new(), BookshelfId.new(), "Salon")
        val copies = ReaderCopiesInMemory()
        copies.visibleTo(lea, ROMANCE_DAWN.id, listOf(onLeasBookshelf, inTheSalon))
        val lookup = lookupAsking(house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) }, copies = copies)

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result.shouldBeInstanceOf<Held>().copies shouldBe listOf(onLeasBookshelf, inTheSalon)
    }

    @Test
    fun `an edition whose copies the reader cannot see is answered with no copy`() {
        // Given
        val juliette = ReaderId.new()
        val copies = ReaderCopiesInMemory()
        val juliettesCopy = CopyOnBookshelf(CopyId.new(), BookshelfId.new(), "Bibliothèque de Juliette")
        copies.visibleTo(juliette, ROMANCE_DAWN.id, listOf(juliettesCopy))
        val source = LookupAnswering(Known(A_PREVIEW.copy(title = "Un titre venu d'une source")))
        val lookup = lookupAsking(
            source,
            house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) },
            copies = copies,
        )

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        val held = result.shouldBeInstanceOf<Held>()
        held.edition shouldBe ROMANCE_DAWN
        held.copies shouldBe emptyList()
        source.asked shouldBe emptyList()
    }

    private fun lookupAsking(
        vararg sources: ExternalEditionLookup,
        house: EditionsInMemory = EditionsInMemory(),
        copies: ReaderCopiesInMemory = ReaderCopiesInMemory(),
    ): LookupIsbnForReader = LookupIsbnForReader(house, copies, LookupEditionByIsbn(sources.toList()))
}
