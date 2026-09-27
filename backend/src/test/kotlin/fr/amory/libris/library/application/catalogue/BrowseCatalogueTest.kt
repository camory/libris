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
import java.util.UUID

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

class BrowseCatalogueTest {
    private val lea = ReaderId.new()
    private val tom = ReaderId.new()
    private val bookshelves = BookshelvesInMemory()
    private val copies = CopiesInMemory()
    private val editions = EditionsInMemory().also {
        it.insert(ROMANCE_DAWN)
        it.insert(BAGGY)
    }
    private val browseCatalogue = BrowseCatalogue(bookshelves, copies, editions)

    @Test
    fun `the catalogue holds the copies on the reader's bookshelves, none of another's`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val tomsBookshelf = bookshelfOf("Bibliothèque de Tom", Membership(tom, OWNER))
        val leasCopy = copyOf(ROMANCE_DAWN, leasBookshelf)
        copyOf(ROMANCE_DAWN, tomsBookshelf)
        copyOf(BAGGY, tomsBookshelf)

        // When
        val held = browseCatalogue(lea, null).held

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
        val held = browseCatalogue(lea, null).held

        // Then
        held.map { it.edition } shouldBe listOf(ROMANCE_DAWN)
        held.single().copies shouldContainExactlyInAnyOrder listOf(
            CopyOnBookshelf(onLeasBookshelf.id, leasBookshelf.id, "Bibliothèque de Léa"),
            CopyOnBookshelf(inTheSalon.id, salon.id, "Salon"),
        )
    }

    @Test
    fun `the catalogue comes in the order of its editions`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        copyOf(BAGGY, leasBookshelf)
        copyOf(ROMANCE_DAWN, leasBookshelf)

        // When
        val held = browseCatalogue(lea, null).held

        // Then
        held.map { it.edition } shouldBe listOf(ROMANCE_DAWN, BAGGY)
    }

    @Test
    fun `the first page holds fifty editions and names the last as next`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val romans = (1..51).map { roman(it) }
        romans.reversed().forEach { copyOf(it, leasBookshelf) }

        // When
        val page = browseCatalogue(lea, null)

        // Then
        page.held.map { it.edition.title } shouldBe (1..50).map { "Roman %02d".format(it) }
        page.next shouldBe romans[49].id
    }

    @Test
    fun `the page after an edition starts just after it`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val romans = (1..51).map { roman(it) }
        romans.reversed().forEach { copyOf(it, leasBookshelf) }

        // When
        val page = browseCatalogue(lea, romans[49].id)

        // Then
        page.held.map { it.edition.title } shouldBe listOf("Roman 51")
        page.next shouldBe null
    }

    @Test
    fun `a page that ends the catalogue names no next`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        (1..50).map { roman(it) }.reversed().forEach { copyOf(it, leasBookshelf) }

        // When
        val page = browseCatalogue(lea, null)

        // Then
        page.held.map { it.edition.title } shouldBe (1..50).map { "Roman %02d".format(it) }
        page.next shouldBe null
    }

    @Test
    fun `an after the catalogue no longer holds continues after its place`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val tomsBookshelf = bookshelfOf("Bibliothèque de Tom", Membership(tom, OWNER))
        copyOf(roman(1), leasBookshelf)
        val roman02 = roman(2)
        copyOf(roman02, tomsBookshelf)
        copyOf(roman(3), leasBookshelf)
        copyOf(roman(4), leasBookshelf)

        // When
        val page = browseCatalogue(lea, roman02.id)

        // Then
        page.held.map { it.edition.title } shouldBe listOf("Roman 03", "Roman 04")
        page.next shouldBe null
    }

    @Test
    fun `an after naming no edition answers an empty page`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        copyOf(roman(1), leasBookshelf)

        // When
        val page = browseCatalogue(lea, EditionId.new())

        // Then
        page shouldBe CataloguePage(emptyList(), null)
    }

    @Test
    fun `editions equal but for their id are split across a page without a repeat or a gap`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        val romans = (1..49).map { roman(it) }
        val lesserTwin = roman(50, EditionId(UUID.fromString("00000000-0000-7000-8000-000000000001")))
        val greaterTwin = roman(50, EditionId(UUID.fromString("00000000-0000-7000-8000-000000000002")))
        copyOf(greaterTwin, leasBookshelf)
        copyOf(lesserTwin, leasBookshelf)
        romans.forEach { copyOf(it, leasBookshelf) }

        // When
        val first = browseCatalogue(lea, null)
        val second = browseCatalogue(lea, first.next)

        // Then
        first.held.map { it.edition } shouldBe romans + lesserTwin
        first.next shouldBe lesserTwin.id
        second.held.map { it.edition } shouldBe listOf(greaterTwin)
        second.next shouldBe null
    }

    @Test
    fun `an edition added between two pages does not shift the next`() {
        // Given
        val leasBookshelf = bookshelfOf("Bibliothèque de Léa", Membership(lea, OWNER))
        (1..51).map { roman(it) }.reversed().forEach { copyOf(it, leasBookshelf) }
        val first = browseCatalogue(lea, null)
        copyOf(roman(0), leasBookshelf)

        // When
        val second = browseCatalogue(lea, first.next)

        // Then
        second.held.map { it.edition.title } shouldBe listOf("Roman 51")
        second.next shouldBe null
    }

    private fun roman(number: Int, id: EditionId = EditionId.new()): Edition = ROMANCE_DAWN.copy(
        id = id,
        isbn = null,
        title = "Roman %02d".format(number),
        series = null,
    ).also { editions.insert(it) }

    private fun bookshelfOf(name: String, vararg memberships: Membership): Bookshelf =
        Bookshelf(BookshelfId.new(), name, memberships.toList()).also { bookshelves.insert(it) }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }
}
