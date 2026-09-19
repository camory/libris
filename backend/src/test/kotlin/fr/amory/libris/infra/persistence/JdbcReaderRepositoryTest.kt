package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole.OWNER
import fr.amory.libris.domain.Reader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@JdbcSliceTest
@Import(JdbcReaderRepository::class, JdbcBookshelfRepository::class)
class JdbcReaderRepositoryTest @Autowired constructor(
    private val readers: JdbcReaderRepository,
    private val bookshelves: JdbcBookshelfRepository,
) {
    @Test
    fun `an inserted reader is found by their username`() {
        // Given
        val juliette = Reader(username = "juliette", email = "juliette@amory.fr", displayName = "Juliette")

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
        readers.insert(Reader(username = "juliette", email = "juliette@amory.fr", displayName = "Juliette"))

        // When, Then
        shouldThrow<DuplicateUsernameException> {
            readers.insert(Reader(username = "juliette", email = "juju@amory.fr", displayName = "Juju"))
        }
    }

    @Test
    fun `an inserted reader is read back with the bookshelf they default to`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Juliette", members = emptyList())
        bookshelves.insert(bookshelf)
        val juliette = Reader(
            username = "juliette",
            email = "juliette@amory.fr",
            displayName = "Juliette",
            defaultBookshelfId = bookshelf.id,
        )

        // When
        readers.insert(juliette)

        // Then
        readers.findByUsername("juliette") shouldBe juliette
    }

    @Test
    fun `an updated reader keeps their columns and takes the bookshelf as their default`() {
        // Given
        val juliette = Reader(username = "juliette", email = "juliette@amory.fr", displayName = "Juliette")
        readers.insert(juliette)
        val bookshelf = Bookshelf(name = "Bibliothèque de Juliette", members = listOf(Member(juliette.id, OWNER)))
        bookshelves.insert(bookshelf)

        // When
        readers.update(juliette.copy(defaultBookshelfId = bookshelf.id))

        // Then
        readers.findByUsername("juliette") shouldBe juliette.copy(defaultBookshelfId = bookshelf.id)
    }
}
