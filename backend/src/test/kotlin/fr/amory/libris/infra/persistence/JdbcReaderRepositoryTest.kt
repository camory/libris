package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Reader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class JdbcReaderRepositoryTest @Autowired constructor(
    private val readers: JdbcReaderRepository,
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
        shouldThrow<DuplicateKeyException> {
            readers.insert(Reader(username = "juliette", email = "juju@amory.fr", displayName = "Juju"))
        }
    }
}
