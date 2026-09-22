package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

private const val ONE_PIECE = "9782723488525"

class AddBookToBookshelfTest {
    private val editions = EditionsInMemory()
    private val copies = CopiesInMemory()
    private val bookshelves = BookshelvesInMemory()

    @Test
    fun `the house lacking the ISBN gains the edition and the copy`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val bookshelf = bookshelfOwnedBy(lea)
        bookshelves.insert(bookshelf)

        // When
        val result = addBookToBookshelf().add(lea.id, bookshelf.id, onePieceTomeOne())

        // Then
        val edition = editions.stored.single()
        edition shouldBe Edition(
            id = edition.id,
            isbn = isbnOf(ONE_PIECE),
            kind = MANGA,
            title = "Romance dawn",
            subtitle = "À l'aube d'une grande aventure",
            contributions = Contributions.of(listOf(Contribution("Eiichiro Oda", WRITER))),
            series = SeriesEntry("One piece", 1),
            collection = "Shonen manga",
            publisher = "Glénat",
            publicationYear = 2013,
            language = "fr",
            pageCount = 208,
            summary = "Luffy prend la mer pour devenir le roi des pirates.",
            coverUrl = "https://covers.libris.test/9782723488525.jpg",
        )
        val copy = copies.stored.single()
        copy shouldBe Copy(copy.id, edition.id, bookshelf.id)
        result shouldBe Added(copy, bookshelf)
    }

    @Test
    fun `an ISBN the house holds reaches the existing edition`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val leasBookshelf = bookshelfOwnedBy(lea)
        bookshelves.insert(leasBookshelf)
        addBookToBookshelf().add(lea.id, leasBookshelf.id, onePieceTomeOne())
        val juliette = readerNamed("juliette", "Juliette")
        val juliettesBookshelf = bookshelfOwnedBy(juliette)
        bookshelves.insert(juliettesBookshelf)

        // When
        val result = addBookToBookshelf().add(juliette.id, juliettesBookshelf.id, onePieceTomeOne())

        // Then
        val edition = editions.stored.single()
        val copy = copies.stored.last()
        copy shouldBe Copy(copy.id, edition.id, juliettesBookshelf.id)
        copies.stored.map { it.editionId } shouldBe listOf(edition.id, edition.id)
        result shouldBe Added(copy, juliettesBookshelf)
    }

    @Test
    fun `the same reader adding it again puts a second copy beside the first`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val bookshelf = bookshelfOwnedBy(lea)
        bookshelves.insert(bookshelf)
        addBookToBookshelf().add(lea.id, bookshelf.id, onePieceTomeOne())

        // When
        addBookToBookshelf().add(lea.id, bookshelf.id, onePieceTomeOne())

        // Then
        val edition = editions.stored.single()
        val (first, second) = copies.stored
        first shouldBe Copy(first.id, edition.id, bookshelf.id)
        second shouldBe Copy(second.id, edition.id, bookshelf.id)
        second.id shouldNotBe first.id
    }

    @Test
    fun `a bookshelf id no bookshelf carries is refused`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val unknown = bookshelfOwnedBy(lea)

        // When
        val result = addBookToBookshelf().add(lea.id, unknown.id, onePieceTomeOne())

        // Then
        result shouldBe NoSuchBookshelf
        editions.stored.shouldBeEmpty()
        copies.stored.shouldBeEmpty()
    }

    @Test
    fun `a bookshelf the reader is not a member of is refused`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val leasBookshelf = bookshelfOwnedBy(lea)
        bookshelves.insert(leasBookshelf)
        val juliette = readerNamed("juliette", "Juliette")

        // When
        val result = addBookToBookshelf().add(juliette.id, leasBookshelf.id, onePieceTomeOne())

        // Then
        result shouldBe NoSuchBookshelf
        editions.stored.shouldBeEmpty()
        copies.stored.shouldBeEmpty()
    }

    private fun addBookToBookshelf(): AddBookToBookshelf = AddBookToBookshelf(editions, copies, bookshelves)

    private fun onePieceTomeOne(): NewBook = NewBook(
        isbn13 = ONE_PIECE,
        kind = MANGA,
        title = "Romance dawn",
        subtitle = "À l'aube d'une grande aventure",
        contributions = Contributions.of(listOf(Contribution("Eiichiro Oda", WRITER))),
        series = SeriesEntry("One piece", 1),
        collection = "Shonen manga",
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        pageCount = 208,
        summary = "Luffy prend la mer pour devenir le roi des pirates.",
        coverUrl = "https://covers.libris.test/9782723488525.jpg",
    )
}
