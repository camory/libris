package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

private const val INSERT_BOOKSHELF = "insert into bookshelf (id, name) values (:id, :name)"

@Repository
class JdbcBookshelfRepository(private val jdbcClient: JdbcClient) : BookshelfRepository {
    override fun insert(bookshelf: Bookshelf) {
        jdbcClient
            .sql(INSERT_BOOKSHELF)
            .param("id", bookshelf.id)
            .param("name", bookshelf.name)
            .update()
    }
}
