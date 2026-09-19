package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient

@JdbcSliceTest
@Import(JdbcBookshelfRepository::class)
class JdbcBookshelfRepositoryTest @Autowired constructor(
    private val bookshelves: JdbcBookshelfRepository,
    private val jdbcClient: JdbcClient,
) {
    @Test
    fun `an inserted bookshelf has its row`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Léa")

        // When
        bookshelves.insert(bookshelf)

        // Then
        val name = jdbcClient
            .sql("select name from bookshelf where id = :id")
            .param("id", bookshelf.id)
            .query(String::class.java)
            .single()
        name shouldBe "Bibliothèque de Léa"
    }
}
