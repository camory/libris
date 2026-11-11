package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.CatalogueQuery
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec
import org.springframework.stereotype.Repository
import java.util.UUID

private const val RANKED =
  """
    WITH ranked AS NOT MATERIALIZED (
        SELECT edition.id,
               edition.isbn13,
               edition.search_vector,
               COALESCE(series.name, edition.title) COLLATE ignoring_case_and_accents AS name,
               edition.volume_number,
               edition.title COLLATE ignoring_case_and_accents AS title
        FROM edition
        LEFT JOIN series ON series.id = edition.series_id
    )
    """

private const val HELD_BY_READER_AFTER =
  """
    EXISTS (
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
    """

private const val IN_CATALOGUE_ORDER =
  """
    ORDER BY ranked.name, ranked.volume_number, ranked.title, ranked.id
    LIMIT :size
    """

private const val FIND_PAGE =
  """
    $RANKED
    SELECT ranked.id
    FROM ranked
    WHERE $HELD_BY_READER_AFTER
    $IN_CATALOGUE_ORDER
    """

private const val FIND_PAGE_OF_ISBN =
  """
    $RANKED
    SELECT ranked.id
    FROM ranked
    WHERE ranked.isbn13 = :isbn13
      AND $HELD_BY_READER_AFTER
    $IN_CATALOGUE_ORDER
    """

private const val FIND_PAGE_OF_WORDS =
  """
    $RANKED
    SELECT ranked.id
    FROM ranked
    WHERE (
          SELECT BOOL_AND(
                     ranked.search_vector @@ CAST(QUOTE_LITERAL(word.lexeme) || ':*' AS tsquery)
                     OR (ranked.volume_number = CASE WHEN word.lexeme ~ '^[0-9]+$' THEN CAST(word.lexeme AS numeric) END) IS TRUE)
          FROM UNNEST(TO_TSVECTOR('words_ignoring_case_and_accents', :words)) AS word
      )
      AND $HELD_BY_READER_AFTER
    $IN_CATALOGUE_ORDER
    """

private const val FIND_FIRST_OF_EACH_SERIES =
  """
    SELECT DISTINCT ON (edition.series_id) edition.id
    FROM edition
    WHERE edition.series_id IS NOT NULL
      AND EXISTS (
          SELECT FROM copy
          JOIN membership ON membership.bookshelf_id = copy.bookshelf_id
          WHERE copy.edition_id = edition.id AND membership.reader_id = :readerId
      )
    ORDER BY edition.series_id, edition.volume_number, edition.title COLLATE ignoring_case_and_accents, edition.id
    """

@Repository
class JdbcCatalogueEditions(private val jdbcClient: JdbcClient) : CatalogueEditions {
  override fun findPage(readerId: ReaderId, after: EditionId?, size: Int): EditionIdPage =
    pageOf(jdbcClient.sql(FIND_PAGE), readerId, after, size)

  override fun findPage(readerId: ReaderId, query: CatalogueQuery, after: EditionId?, size: Int): EditionIdPage =
    pageOf(searchOf(query), readerId, after, size)

  override fun findFirstOfEachSeries(readerId: ReaderId): Set<EditionId> =
    jdbcClient
      .sql(FIND_FIRST_OF_EACH_SERIES)
      .param("readerId", readerId.value)
      .query { rs, _ -> EditionId(rs.getObject("id", UUID::class.java)) }
      .set()

  private fun searchOf(query: CatalogueQuery): StatementSpec =
    query.isbn
      ?.let { jdbcClient.sql(FIND_PAGE_OF_ISBN).param("isbn13", it.digits) }
      ?: jdbcClient.sql(FIND_PAGE_OF_WORDS).param("words", query.text)

  private fun pageOf(statement: StatementSpec, readerId: ReaderId, after: EditionId?, size: Int): EditionIdPage {
    val following = statement
      .param("readerId", readerId.value)
      .param("after", after?.value)
      .param("size", size + 1)
      .query { rs, _ -> EditionId(rs.getObject("id", UUID::class.java)) }
      .list()
    val page = following.take(size)
    return EditionIdPage(page, if (following.size > size) page.last() else null)
  }
}
