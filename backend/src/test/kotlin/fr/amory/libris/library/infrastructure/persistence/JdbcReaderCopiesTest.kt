package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

private const val ONE_PIECE = "9782723488525"

@JdbcSliceTest
@Import(
    JdbcReaderCopies::class,
    JdbcCopyRepository::class,
    JdbcEditionRepository::class,
    JdbcBookshelfRepository::class,
    JdbcReaderRepository::class,
)
class JdbcReaderCopiesTest @Autowired constructor(
    private val readerCopies: JdbcReaderCopies,
    private val copies: JdbcCopyRepository,
    private val editions: JdbcEditionRepository,
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
) {
    @Test
    fun `a copy on the reader's own bookshelf comes with the name of that bookshelf`() {
        // Given
        val lea = readerOf("lea", "Léa")
        val bookshelf = bookshelfOf(lea)
        val edition = editionOf(ONE_PIECE, "Romance dawn")
        val copy = copyOf(edition, bookshelf)

        // When
        val visible = readerCopies.ofEdition(edition.id, lea.id)

        // Then
        visible shouldBe listOf(CopyOnBookshelf(copy.id, bookshelf.id, "Bibliothèque de Léa"))
    }

    @Test
    fun `a copy on a bookshelf the reader is not a member of is not answered`() {
        // Given
        val lea = readerOf("lea", "Léa")
        val leasBookshelf = bookshelfOf(lea)
        val juliette = readerOf("juliette", "Juliette")
        val juliettesBookshelf = bookshelfOf(juliette)
        val edition = editionOf(ONE_PIECE, "Romance dawn")
        val leasCopy = copyOf(edition, leasBookshelf)
        copyOf(edition, juliettesBookshelf)

        // When
        val visible = readerCopies.ofEdition(edition.id, lea.id)

        // Then
        visible shouldBe listOf(CopyOnBookshelf(leasCopy.id, leasBookshelf.id, "Bibliothèque de Léa"))
    }

    private fun readerOf(username: String, displayName: String): Reader =
        readerNamed(username, displayName).also { readers.insert(it) }

    private fun bookshelfOf(reader: Reader): Bookshelf = bookshelfOwnedBy(reader).also { bookshelves.insert(it) }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }

    private fun editionOf(isbn: String, title: String): Edition = Edition(
        id = EditionId.new(),
        isbn = isbnOf(isbn),
        kind = MANGA,
        title = title,
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
    ).also { editions.insert(it) }
}
