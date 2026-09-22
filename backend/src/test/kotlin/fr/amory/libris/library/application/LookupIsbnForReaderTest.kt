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
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CopiesInMemory
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
    private val copies = CopiesInMemory()
    private val bookshelves = BookshelvesInMemory()

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
    fun `the copies on the reader's bookshelves come with the edition, by the name of their bookshelf`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val salon = bookshelfOf("Salon", Membership(ReaderId.new(), OWNER), Membership(lea, VIEWER))
        val inTheSalon = copyOf(ROMANCE_DAWN, salon)
        val onLeasBookshelf = copyOf(ROMANCE_DAWN, leasBookshelf)
        val lookup = lookupAsking(house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) })

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result.shouldBeInstanceOf<Held>().copies shouldBe listOf(
            CopyView(onLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa"),
            CopyView(inTheSalon.id, salon.id, "Salon"),
        )
    }

    @Test
    fun `an edition whose copies sit on bookshelves the reader is not a member of is answered with no copy`() {
        // Given
        val juliette = ReaderId.new()
        val juliettesBookshelf = bookshelfOf("Bibliothèque de Juliette", Membership(juliette, OWNER))
        copyOf(ROMANCE_DAWN, juliettesBookshelf)
        val source = LookupAnswering(Known(A_PREVIEW.copy(title = "Un titre venu d'une source")))
        val lookup = lookupAsking(source, house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) })

        // When
        val result = lookup.lookUp(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe Held(ROMANCE_DAWN, emptyList())
        source.asked shouldBe emptyList()
    }

    private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
        Bookshelf(BookshelfId.new(), name, memberships.toList()).also { bookshelves.insert(it) }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }

    private fun lookupAsking(
        vararg sources: ExternalEditionLookup,
        house: EditionsInMemory = EditionsInMemory(),
    ): LookupIsbnForReader = LookupIsbnForReader(house, copies, bookshelves, LookupEditionByIsbn(sources.toList()))
}
