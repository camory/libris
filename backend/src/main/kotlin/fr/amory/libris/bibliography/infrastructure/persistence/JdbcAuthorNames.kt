package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.edition.AuthorNames
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

private const val FIND_ALL_AUTHOR_NAMES =
  """
    SELECT author.name FROM author
    WHERE EXISTS (SELECT 1 FROM contribution WHERE contribution.author_id = author.id)
    """

@Repository
class JdbcAuthorNames(private val jdbcClient: JdbcClient) : AuthorNames {
  override fun findAll(): List<String> =
    jdbcClient
      .sql(FIND_ALL_AUTHOR_NAMES)
      .query { rs, _ -> rs.getString("name") }
      .list()
}
