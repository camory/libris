package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.series.CarriedSeries
import fr.amory.libris.bibliography.domain.series.SeriesId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val FIND_ALL_CARRIED_SERIES =
  "SELECT edition.series_id FROM edition"

@Repository
class JdbcCarriedSeries(private val jdbcClient: JdbcClient) : CarriedSeries {
  override fun findAll(): Set<SeriesId> =
    jdbcClient
      .sql(FIND_ALL_CARRIED_SERIES)
      .query { rs, _ -> SeriesId(rs.getObject("series_id", UUID::class.java)) }
      .set()
}
