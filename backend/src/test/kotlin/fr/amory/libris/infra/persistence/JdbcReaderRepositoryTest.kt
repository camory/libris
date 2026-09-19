package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.fixture.readerOwning
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.simple.JdbcClient

@JdbcSliceTest
@Import(JdbcReaderRepository::class, JdbcBookshelfRepository::class)
class JdbcReaderRepositoryTest @Autowired constructor(
    private val readers: JdbcReaderRepository,
    private val bookshelves: JdbcBookshelfRepository,
    private val jdbcClient: JdbcClient,
) {
    @Test
    fun `an inserted reader is read back whole, the bookshelves they are a member of included`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Juliette")
        bookshelves.insert(bookshelf)
        val juliette = readerOwning(bookshelf, "juliette", "Juliette")

        // When
        readers.insert(juliette)

        // Then
        readers.findByUsername("juliette") shouldBe juliette
    }

    @Test
    fun `an unknown username finds no reader`() {
        readers.findByUsername("nobody") shouldBe null
    }

    @Test
    fun `a second reader with the same username is refused`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Juliette")
        bookshelves.insert(bookshelf)
        readers.insert(readerOwning(bookshelf, "juliette", "Juliette"))

        // When, Then
        shouldThrow<DuplicateUsernameException> {
            readers.insert(readerOwning(bookshelf, "juliette", "Juju", email = "juju@amory.fr"))
        }
    }

    @Test
    fun `a member of a role the product does not know is refused`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Juliette")
        bookshelves.insert(bookshelf)
        val juliette = readerOwning(bookshelf, "juliette", "Juliette")
        readers.insert(juliette)
        val another = Bookshelf(name = "Bibliothèque de Léa")
        bookshelves.insert(another)

        // When, Then
        shouldThrow<DataIntegrityViolationException> {
            jdbcClient
                .sql("insert into reader_bookshelf (reader_id, bookshelf_id, role) values (:reader, :bookshelf, 'LENDER')")
                .param("reader", juliette.id)
                .param("bookshelf", another.id)
                .update()
        }
    }
}
