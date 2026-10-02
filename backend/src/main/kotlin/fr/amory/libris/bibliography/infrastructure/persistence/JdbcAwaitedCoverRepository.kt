package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository

private const val INSERT_AWAITED_COVER =
  """
    INSERT INTO awaited_cover (isbn13, source)
    VALUES (:isbn13, :source)
    """

@Repository
class JdbcAwaitedCoverRepository(private val jdbcClient: JdbcClient) : AwaitedCoverRepository {
  override fun insert(awaitedCover: AwaitedCover) {
    jdbcClient
      .sql(INSERT_AWAITED_COVER)
      .param("isbn13", awaitedCover.isbn.digits)
      .param("source", awaitedCover.chosenSource?.label)
      .update()
  }
}
