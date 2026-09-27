package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
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
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private val ROMANCE_DAWN = Edition(
    id = EditionId.new(),
    isbn = isbnOf("9782723488525"),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = "à l'aube d'une grande aventure",
    contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
    series = SeriesEntry("One piece", 1),
    collection = "Shonen manga",
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 203,
    summary = "Le début de l'aventure.",
    coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
)

private val BAGGY = ROMANCE_DAWN.copy(
    id = EditionId.new(),
    isbn = isbnOf("9782723489898"),
    title = "Aux prises avec Baggy et ses hommes",
    series = SeriesEntry("One piece", 2),
)

class ListCatalogueTest {
    private val lea = ReaderId.new()
    private val tom = ReaderId.new()
    private val bookshelves = BookshelvesInMemory()
    private val copies = CopiesInMemory()
    private val editions = EditionsInMemory().also {
        it.insert(ROMANCE_DAWN)
        it.insert(BAGGY)
    }
    private val listCatalogue = ListCatalogue(bookshelves, copies, editions)

    @Test
    fun `the catalogue holds the copies on the reader's bookshelves, none of another's`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val tomsBookshelf = bookshelfOf("Bibliothèque de Tom", Membership(tom, OWNER))
        val leasCopy = copyOf(ROMANCE_DAWN, leasBookshelf)
        copyOf(ROMANCE_DAWN, tomsBookshelf)
        copyOf(BAGGY, tomsBookshelf)

        // When
        val held = listCatalogue(lea)

        // Then
        held shouldBe listOf(
            HeldEdition(ROMANCE_DAWN, listOf(CopyOnBookshelf(leasCopy.id, leasBookshelf.id, "Bibliothèque de Léa"))),
        )
    }

    @Test
    fun `an edition on two of the reader's bookshelves comes once`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val salon = bookshelfOf("Salon", Membership(tom, OWNER), Membership(lea, VIEWER))
        val onLeasBookshelf = copyOf(ROMANCE_DAWN, leasBookshelf)
        val inTheSalon = copyOf(ROMANCE_DAWN, salon)

        // When
        val held = listCatalogue(lea)

        // Then
        held.map { it.edition } shouldBe listOf(ROMANCE_DAWN)
        held.single().copies shouldContainExactlyInAnyOrder listOf(
            CopyOnBookshelf(onLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa"),
            CopyOnBookshelf(inTheSalon.id, salon.id, "Salon"),
        )
    }

    private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
        Bookshelf(BookshelfId.new(), name, memberships.toList()).also { bookshelves.insert(it) }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }
}
