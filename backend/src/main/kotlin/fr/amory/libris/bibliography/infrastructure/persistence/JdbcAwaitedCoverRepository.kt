package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository
import fr.amory.libris.bibliography.domain.cover.CoverSource
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

private const val INSERT_AWAITED_COVER =
  """
    INSERT INTO awaited_cover (isbn13, source)
    VALUES (:isbn13, :source)
    """

private const val UPDATE_AWAITED_COVER =
  """
    UPDATE awaited_cover SET source = :source, attempted_at = :attemptedAt
    WHERE awaited_cover.isbn13 = :isbn13
    """

private const val SELECT_AWAITED_COVERS =
  "SELECT awaited_cover.isbn13, awaited_cover.source, awaited_cover.attempted_at FROM awaited_cover"

private const val DELETE_AWAITED_COVER =
  "DELETE FROM awaited_cover WHERE awaited_cover.isbn13 = :isbn13"

@Repository
class JdbcAwaitedCoverRepository(private val jdbcClient: JdbcClient) : AwaitedCoverRepository {
  override fun insert(awaitedCover: AwaitedCover) {
    jdbcClient
      .sql(INSERT_AWAITED_COVER)
      .param("isbn13", awaitedCover.isbn.digits)
      .param("source", awaitedCover.chosenSource?.label)
      .update()
  }

  override fun update(awaitedCover: AwaitedCover) {
    jdbcClient
      .sql(UPDATE_AWAITED_COVER)
      .param("isbn13", awaitedCover.isbn.digits)
      .param("source", awaitedCover.chosenSource?.label)
      .param("attemptedAt", awaitedCover.attemptedAt?.atOffset(UTC))
      .update()
  }

  override fun findAll(): List<AwaitedCover> =
    jdbcClient
      .sql(SELECT_AWAITED_COVERS)
      .query { rs, _ -> awaitedCoverOf(rs) }
      .list()
      .filterNotNull()

  override fun delete(awaitedCover: AwaitedCover) {
    jdbcClient
      .sql(DELETE_AWAITED_COVER)
      .param("isbn13", awaitedCover.isbn.digits)
      .update()
  }

  private fun awaitedCoverOf(rs: ResultSet): AwaitedCover? =
    Isbn
      .of(rs.getString("isbn13"))
      ?.let {
        AwaitedCover(
          it,
          rs.getString("source")?.let(CoverSource::of),
          rs.getObject("attempted_at", OffsetDateTime::class.java)?.toInstant(),
        )
      }
}
