package fr.amory.libris.library.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
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
    subtitle = null,
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

class LookupIsbnForReaderTest {
    private val lea = ReaderId.new()
    private val copies = CopiesInMemory()
    private val bookshelves = BookshelvesInMemory()

    @Test
    fun `an ISBN the house lacks is answered by the sources, with no copy`() {
        // Given
        val lookupIsbnForReader = lookupAsking(LookupAnswering(Known(A_PREVIEW.copy(title = "Romance dawn"))))

        // When
        val result = lookupIsbnForReader(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe IsbnLookup(Found(A_PREVIEW.copy(title = "Romance dawn")), emptyList())
    }

    @Test
    fun `an ISBN no source knows is answered unknown, with no copy`() {
        // Given
        val lookupIsbnForReader = lookupAsking(LookupAnswering(NothingKnown))

        // When
        val result = lookupIsbnForReader(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe IsbnLookup(UnknownIsbn, emptyList())
    }

    @Test
    fun `an ISBN whose sources are all down is answered sources unavailable, with no copy`() {
        // Given
        val lookupIsbnForReader = lookupAsking(LookupAnswering(Failed))

        // When
        val result = lookupIsbnForReader(lea, isbnOf(ONE_PIECE))

        // Then
        result shouldBe IsbnLookup(SourcesUnavailable, emptyList())
    }

    @Test
    fun `the copies on the reader's bookshelves come with the edition, by the name of their bookshelf`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val salon = bookshelfOf("Salon", Membership(ReaderId.new(), OWNER), Membership(lea, VIEWER))
        val inTheSalon = copyOf(ROMANCE_DAWN, salon)
        val onLeasBookshelf = copyOf(ROMANCE_DAWN, leasBookshelf)
        val lookupIsbnForReader = lookupAsking(house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) })

        // When
        val result = lookupIsbnForReader(lea, isbnOf(ONE_PIECE))

        // Then
        result.copies shouldBe listOf(
            CopyOnBookshelf(onLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa"),
            CopyOnBookshelf(inTheSalon.id, salon.id, "Salon"),
        )
    }

    @Test
    fun `an edition whose copies sit on bookshelves the reader is not a member of is answered with no copy`() {
        // Given
        val juliette = ReaderId.new()
        val juliettesBookshelf = bookshelfOf("Bibliothèque de Juliette", Membership(juliette, OWNER))
        copyOf(ROMANCE_DAWN, juliettesBookshelf)
        val lookupIsbnForReader = lookupAsking(house = EditionsInMemory().also { it.insert(ROMANCE_DAWN) })

        // When
        val result = lookupIsbnForReader(lea, isbnOf(ONE_PIECE))

        // Then
        result.answer.shouldBeInstanceOf<Held>().id shouldBe ROMANCE_DAWN.id
        result.copies shouldBe emptyList()
    }

    private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
        Bookshelf(BookshelfId.new(), name, memberships.toList()).also { bookshelves.insert(it) }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }

    private fun lookupAsking(
        vararg sources: ExternalEditionLookup,
        house: EditionsInMemory = EditionsInMemory(),
    ): LookupIsbnForReader = LookupIsbnForReader(LookupEditionByIsbn(house, sources.toList()), copies, bookshelves)
}
