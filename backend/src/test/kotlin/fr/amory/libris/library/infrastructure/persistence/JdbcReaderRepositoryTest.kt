package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@JdbcSliceTest
@Import(JdbcReaderRepository::class)
class JdbcReaderRepositoryTest @Autowired constructor(
    private val readers: JdbcReaderRepository,
) {
    @Test
    fun `an inserted reader is read back whole`() {
        // Given
        val juliette = readerNamed("juliette", "Juliette")

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
        readers.insert(readerNamed("juliette", "Juliette"))

        // When, Then
        shouldThrow<DuplicateUsernameException> {
            readers.insert(readerNamed("juliette", "Juju", email = "juju@amory.fr"))
        }
    }
}
