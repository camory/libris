package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient
import java.util.UUID

private const val ONE_PIECE = "9782723488525"

@JdbcSliceTest
@Import(
    JdbcCopyRepository::class,
    JdbcEditionRepository::class,
    JdbcBookshelfRepository::class,
    JdbcReaderRepository::class,
)
class JdbcCopyRepositoryTest @Autowired constructor(
    private val copies: JdbcCopyRepository,
    private val editions: JdbcEditionRepository,
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
    private val jdbcClient: JdbcClient,
) {
    @Test
    fun `a copy inserted sits on its bookshelf and its edition`() {
        // Given
        val bookshelf = bookshelfOf("lea", "Léa")
        val edition = onePieceTomeOne()
        editions.insert(edition)
        val copy = Copy(CopyId.new(), edition.id, bookshelf.id)

        // When
        copies.insert(copy)

        // Then
        rowOf(copy.id) shouldBe (edition.id.value to bookshelf.id.value)
    }

    @Test
    fun `two copies of one edition on one bookshelf stand beside each other`() {
        // Given
        val bookshelf = bookshelfOf("lea", "Léa")
        val edition = onePieceTomeOne()
        editions.insert(edition)
        val first = Copy(CopyId.new(), edition.id, bookshelf.id)
        copies.insert(first)

        // When
        val second = Copy(CopyId.new(), edition.id, bookshelf.id)
        copies.insert(second)

        // Then
        rowOf(first.id) shouldBe (edition.id.value to bookshelf.id.value)
        rowOf(second.id) shouldBe (edition.id.value to bookshelf.id.value)
    }

    @Test
    fun `a copy of an edition the house does not hold is refused`() {
        // Given
        val bookshelf = bookshelfOf("lea", "Léa")

        // When
        val copy = Copy(CopyId.new(), EditionId.new(), bookshelf.id)

        // Then
        shouldThrow<DataIntegrityViolationException> { copies.insert(copy) }
    }

    @Test
    fun `a copy on a bookshelf Libris does not know is refused`() {
        // Given
        val edition = onePieceTomeOne()
        editions.insert(edition)

        // When
        val copy = Copy(CopyId.new(), edition.id, BookshelfId.new())

        // Then
        shouldThrow<DataIntegrityViolationException> { copies.insert(copy) }
    }

    private fun bookshelfOf(username: String, displayName: String): Bookshelf {
        val reader = readerNamed(username, displayName)
        readers.insert(reader)
        val bookshelf = bookshelfOwnedBy(reader)
        bookshelves.insert(bookshelf)
        return bookshelf
    }

    private fun rowOf(id: CopyId): Pair<UUID, UUID>? =
        jdbcClient
            .sql("SELECT copy.edition_id, copy.bookshelf_id FROM copy WHERE copy.id = :id")
            .param("id", id.value)
            .query { rs, _ ->
                rs.getObject("edition_id", UUID::class.java) to rs.getObject("bookshelf_id", UUID::class.java)
            }
            .list()
            .singleOrNull()

    private fun onePieceTomeOne(): Edition = Edition(
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
}
