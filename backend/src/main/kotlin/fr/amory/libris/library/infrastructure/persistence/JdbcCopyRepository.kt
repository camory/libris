package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

private const val INSERT_COPY =
    "INSERT INTO copy (id, edition_id, bookshelf_id) VALUES (:id, :editionId, :bookshelfId)"

@Repository
class JdbcCopyRepository(private val jdbcClient: JdbcClient) : CopyRepository {
    override fun insert(copy: Copy) {
        jdbcClient
            .sql(INSERT_COPY)
            .param("id", copy.id.value)
            .param("editionId", copy.editionId.value)
            .param("bookshelfId", copy.bookshelfId.value)
            .update()
    }
}
