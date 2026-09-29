package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CatalogueEditionsAnswering
import fr.amory.libris.library.fixture.CopiesInMemory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private val CIGARES = Edition(
    id = EditionId.new(),
    isbn = null,
    kind = BD,
    title = "Les cigares du pharaon",
    subtitle = null,
    contributions = Contributions.of(emptyList()),
    series = SeriesEntry("Tintin", 1),
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
)

private val PIERRE = CIGARES.copy(
    id = EditionId.new(),
    kind = BOOK,
    title = "Pierre et le loup",
    series = null,
)

class BrowseCatalogueTest {
    private val lea = ReaderId.new()
    private val tom = ReaderId.new()
    private val bookshelves = BookshelvesInMemory()
    private val copies = CopiesInMemory()
    private val editions = EditionsInMemory().also {
        it.insert(CIGARES)
        it.insert(PIERRE)
    }

    @Test
    fun `the catalogue answers the port's editions in its order, with the reader's copies and its next`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val salon = bookshelfOf("Salon", Membership(tom, OWNER), Membership(lea, VIEWER))
        val pierreOnLeasBookshelf = copyOf(PIERRE, leasBookshelf)
        val cigaresOnLeasBookshelf = copyOf(CIGARES, leasBookshelf)
        val cigaresInTheSalon = copyOf(CIGARES, salon)
        val next = EditionId.new()
        val browseCatalogue = browsing(EditionIdPage(listOf(CIGARES.id, PIERRE.id), next))

        // When
        val page = browseCatalogue(lea, null)

        // Then
        page shouldBe CataloguePage(
            listOf(
                HeldEdition(
                    CIGARES,
                    listOf(
                        CopyOnBookshelf(cigaresOnLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa"),
                        CopyOnBookshelf(cigaresInTheSalon.id, salon.id, "Salon"),
                    ),
                ),
                HeldEdition(
                    PIERRE,
                    listOf(CopyOnBookshelf(pierreOnLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa")),
                ),
            ),
            next,
        )
    }

    private fun browsing(answer: EditionIdPage): BrowseCatalogue =
        BrowseCatalogue(CatalogueEditionsAnswering(answer), bookshelves, copies, editions)

    private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
        Bookshelf(BookshelfId.new(), name, memberships.toList()).also { bookshelves.insert(it) }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }
}
