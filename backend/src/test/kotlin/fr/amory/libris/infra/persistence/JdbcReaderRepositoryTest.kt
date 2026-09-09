package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Reader
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
}
