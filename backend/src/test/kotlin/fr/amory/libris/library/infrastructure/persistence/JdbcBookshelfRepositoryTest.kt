package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException

@JdbcSliceTest
@Import(JdbcBookshelfRepository::class, JdbcReaderRepository::class)
class JdbcBookshelfRepositoryTest @Autowired constructor(
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
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

        readers.insert(lea)
        readers.insert(juliette)
        bookshelves.insert(bookshelfOwnedBy(juliette))

        // When
        bookshelves.insert(bookshelf)

        // Then
        val read = requireNotNull(bookshelves.findById(bookshelf.id))
        read.memberships.toSet() shouldBe bookshelf.memberships.toSet()
        read.copy(memberships = bookshelf.memberships) shouldBe bookshelf
    }

    @Test
    fun `a membership of a reader Libris does not know is refused`() {
        val nobody = readerNamed("nobody", "Nobody")

        shouldThrow<DataIntegrityViolationException> { bookshelves.insert(bookshelfOwnedBy(nobody)) }
    }

    @Test
    fun `an unknown id finds no bookshelf`() {
        bookshelves.findById(BookshelfId.new()) shouldBe null
    }
}
