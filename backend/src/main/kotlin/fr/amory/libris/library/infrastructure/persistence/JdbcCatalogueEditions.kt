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
    WITH ranked AS NOT MATERIALIZED (
        SELECT edition.id,
               COALESCE(series.name, edition.title) COLLATE ignoring_case_and_accents AS name,
               edition.volume_number,
               edition.title COLLATE ignoring_case_and_accents AS title
        FROM edition
        LEFT JOIN series ON series.id = edition.series_id
    )
    SELECT ranked.id
    FROM ranked
    WHERE EXISTS (
          SELECT FROM copy
          JOIN membership ON membership.bookshelf_id = copy.bookshelf_id
          WHERE copy.edition_id = ranked.id AND membership.reader_id = :readerId
      )
      AND (CAST(:after AS uuid) IS NULL OR EXISTS (
          SELECT FROM ranked AS place
          WHERE place.id = :after
            AND (ranked.name > place.name
                 OR ranked.name = place.name
                    AND (ranked.volume_number > place.volume_number
                         OR ranked.volume_number IS NULL AND place.volume_number IS NOT NULL
                         OR ranked.volume_number IS NOT DISTINCT FROM place.volume_number
                            AND (ranked.title > place.title
                                 OR ranked.title = place.title AND ranked.id > place.id)))
      ))
    ORDER BY ranked.name, ranked.volume_number, ranked.title, ranked.id
    LIMIT :size
    """

@Repository
class JdbcCatalogueEditions(private val jdbcClient: JdbcClient) : CatalogueEditions {
  override fun findPage(readerId: ReaderId, after: EditionId?, size: Int): EditionIdPage {
    val following = jdbcClient
      .sql(FIND_PAGE)
      .param("readerId", readerId.value)
      .param("after", after?.value)
      .param("size", size + 1)
      .query { rs, _ -> EditionId(rs.getObject("id", UUID::class.java)) }
      .list()
    val page = following.take(size)
    return EditionIdPage(page, if (following.size > size) page.last() else null)
  }
}
