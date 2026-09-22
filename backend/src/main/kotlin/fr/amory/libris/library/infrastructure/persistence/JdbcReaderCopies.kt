package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.lookup.ReaderCopies
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val FIND_COPIES_OF_EDITION =
    """
    SELECT copy.id, bookshelf.id AS bookshelf_id, bookshelf.name
    FROM copy
    JOIN bookshelf ON bookshelf.id = copy.bookshelf_id
    WHERE copy.edition_id = :editionId
    """

@Repository
class JdbcReaderCopies(private val jdbcClient: JdbcClient) : ReaderCopies {
    override fun ofEdition(editionId: EditionId, readerId: ReaderId): List<CopyOnBookshelf> =
        jdbcClient
            .sql(FIND_COPIES_OF_EDITION)
            .param("editionId", editionId.value)
            .query { rs, _ ->
                CopyOnBookshelf(
                    copyId = CopyId(rs.getObject("id", UUID::class.java)),
                    bookshelfId = BookshelfId(rs.getObject("bookshelf_id", UUID::class.java)),
                    bookshelfName = rs.getString("name"),
                )
            }
            .list()
}
