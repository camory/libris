package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.CopyRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_COPY =
    "INSERT INTO copy (id, edition_id, bookshelf_id) VALUES (:id, :editionId, :bookshelfId)"

private const val FIND_COPIES_BY_EDITION =
    "SELECT copy.id, copy.edition_id, copy.bookshelf_id FROM copy WHERE copy.edition_id = :editionId"

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

    override fun findByEditionId(editionId: EditionId): List<Copy> =
        jdbcClient
            .sql(FIND_COPIES_BY_EDITION)
            .param("editionId", editionId.value)
            .query { rs, _ ->
                Copy(
                    id = CopyId(rs.getObject("id", UUID::class.java)),
                    editionId = EditionId(rs.getObject("edition_id", UUID::class.java)),
                    bookshelfId = BookshelfId(rs.getObject("bookshelf_id", UUID::class.java)),
                )
            }
            .list()
}
