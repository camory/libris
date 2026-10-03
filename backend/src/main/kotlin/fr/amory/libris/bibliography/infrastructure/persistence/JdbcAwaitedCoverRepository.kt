package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository
import fr.amory.libris.bibliography.domain.cover.CoverSource
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet

private const val INSERT_AWAITED_COVER =
  """
    INSERT INTO awaited_cover (isbn13, source)
    VALUES (:isbn13, :source)
    """

private const val SELECT_AWAITED_COVERS =
  "SELECT awaited_cover.isbn13, awaited_cover.source FROM awaited_cover"

@Repository
class JdbcAwaitedCoverRepository(private val jdbcClient: JdbcClient) : AwaitedCoverRepository {
  override fun insert(awaitedCover: AwaitedCover) {
    jdbcClient
      .sql(INSERT_AWAITED_COVER)
      .param("isbn13", awaitedCover.isbn.digits)
      .param("source", awaitedCover.chosenSource?.label)
      .update()
  }

  override fun findAll(): List<AwaitedCover> =
    jdbcClient
      .sql(SELECT_AWAITED_COVERS)
      .query { rs, _ -> awaitedCoverOf(rs) }
      .list()
      .filterNotNull()

  private fun awaitedCoverOf(rs: ResultSet): AwaitedCover? =
    Isbn
      .of(rs.getString("isbn13"))
      ?.let { AwaitedCover(it, rs.getString("source")?.let(CoverSource::of)) }
}
