package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Reader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.UUID

@JdbcSliceTest
@Import(JdbcReaderRepository::class, JdbcBookshelfRepository::class)
class JdbcReaderRepositoryTest @Autowired constructor(
    private val readers: JdbcReaderRepository,
    private val bookshelves: JdbcBookshelfRepository,
) {
    @Test
    fun `an inserted reader is read back with the bookshelf they default to`() {
        // Given
        val juliette = Reader(
            username = "juliette",
            email = "juliette@amory.fr",
            displayName = "Juliette",
            defaultBookshelfId = aBookshelf(),
        )

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
        val bookshelf = aBookshelf()
        readers.insert(
            Reader(username = "juliette", email = "juliette@amory.fr", displayName = "Juliette", defaultBookshelfId = bookshelf),
        )

        // When, Then
        shouldThrow<DuplicateUsernameException> {
            readers.insert(
                Reader(username = "juliette", email = "juju@amory.fr", displayName = "Juju", defaultBookshelfId = bookshelf),
            )
        }
    }

    private fun aBookshelf(): UUID {
        val bookshelf = Bookshelf(name = "Bibliothèque de Juliette", members = emptyList())
        bookshelves.insert(bookshelf)
        return bookshelf.id
    }
}
