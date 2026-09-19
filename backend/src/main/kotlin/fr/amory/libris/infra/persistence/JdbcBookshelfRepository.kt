package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_BOOKSHELF = "insert into bookshelf (id, name) values (:id, :name)"

private const val FIND_BOOKSHELF_BY_ID = "select id, name from bookshelf where id = :id"

@Repository
class JdbcBookshelfRepository(private val jdbcClient: JdbcClient) : BookshelfRepository {
    override fun insert(bookshelf: Bookshelf) {
        jdbcClient
            .sql(INSERT_BOOKSHELF)
            .param("id", bookshelf.id)
            .param("name", bookshelf.name)
            .update()
    }

    override fun findById(id: UUID): Bookshelf? =
        jdbcClient
            .sql(FIND_BOOKSHELF_BY_ID)
            .param("id", id)
            .query { rs, _ -> Bookshelf(id = rs.getObject("id", UUID::class.java), name = rs.getString("name")) }
            .optional()
            .orElse(null)
}
