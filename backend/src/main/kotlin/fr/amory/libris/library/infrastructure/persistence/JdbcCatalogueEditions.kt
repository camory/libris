package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val FIND_PAGE =
    """
    SELECT copy.edition_id
    FROM membership
    JOIN copy ON copy.bookshelf_id = membership.bookshelf_id
    WHERE membership.reader_id = :readerId
    LIMIT :size
    """

@Repository
class JdbcCatalogueEditions(private val jdbcClient: JdbcClient) : CatalogueEditions {
    override fun findPage(readerId: ReaderId, after: EditionId?, size: Int): EditionIdPage =
        EditionIdPage(
            jdbcClient
                .sql(FIND_PAGE)
                .param("readerId", readerId.value)
                .param("size", size)
                .query { rs, _ -> EditionId(rs.getObject("edition_id", UUID::class.java)) }
                .list(),
            null,
        )
}
