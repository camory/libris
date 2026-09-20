package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient

@JdbcSliceTest
@Import(JdbcBookshelfRepository::class, JdbcReaderRepository::class)
class JdbcBookshelfRepositoryTest @Autowired constructor(
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
    private val jdbcClient: JdbcClient,
) {
    @Test
    fun `an inserted bookshelf is read back whole, its members included`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val juliette = readerNamed("juliette", "Juliette")
        val bookshelf = Bookshelf(
            id = lea.defaultBookshelfId,
            name = "Bibliothèque de Léa",
            memberships = listOf(Membership(lea.id, OWNER), Membership(juliette.id, VIEWER)),
        )

        // When
        bookshelves.insert(bookshelf)
        readers.insert(lea)
        bookshelves.insert(bookshelfOwnedBy(juliette))
        readers.insert(juliette)

        // Then
        val read = requireNotNull(bookshelves.findById(bookshelf.id))
        read.memberships.toSet() shouldBe bookshelf.memberships.toSet()
        read.copy(memberships = bookshelf.memberships) shouldBe bookshelf
    }

    @Test
    fun `an unknown id finds no bookshelf`() {
        bookshelves.findById(BookshelfId.new()) shouldBe null
    }

    @Test
    fun `a member of a role the product does not know is refused`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        bookshelves.insert(bookshelfOwnedBy(lea))
        readers.insert(lea)

        // When, Then
        shouldThrow<DataIntegrityViolationException> {
            jdbcClient
                .sql("insert into membership (bookshelf_id, reader_id, role) values (:bookshelf, :reader, :role)")
                .param("bookshelf", lea.defaultBookshelfId.value)
                .param("reader", ReaderId.new().value)
                .param("role", "LENDER")
                .update()
        }
    }
}
