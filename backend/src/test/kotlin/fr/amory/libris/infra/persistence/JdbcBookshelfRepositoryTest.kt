package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.util.UUID

@JdbcSliceTest
@Import(JdbcBookshelfRepository::class)
class JdbcBookshelfRepositoryTest @Autowired constructor(
    private val bookshelves: JdbcBookshelfRepository,
) {
    @Test
    fun `an inserted bookshelf is found by its id`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Léa")

        // When
        bookshelves.insert(bookshelf)

        // Then
        bookshelves.findById(bookshelf.id) shouldBe bookshelf
    }

    @Test
    fun `an unknown id finds no bookshelf`() {
        bookshelves.findById(UUID.randomUUID()) shouldBe null
    }
}
