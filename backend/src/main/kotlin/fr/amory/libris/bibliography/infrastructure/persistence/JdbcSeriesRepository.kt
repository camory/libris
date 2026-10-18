package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

private const val INSERT_SERIES =
  """
    INSERT INTO series (id, name) VALUES (:id, :name)
    ON CONFLICT (LOWER(name)) DO UPDATE SET name = series.name
    RETURNING id, name
    """

private const val FIND_SERIES_BY_ID =
  "SELECT series.id, series.name FROM series WHERE series.id = :id"

private const val FIND_SERIES_BY_IDS =
  "SELECT series.id, series.name FROM series WHERE series.id IN (:ids)"

@Repository
class JdbcSeriesRepository(private val jdbcClient: JdbcClient) : SeriesRepository {
  override fun findOrInsert(series: Series): Series =
    jdbcClient
      .sql(INSERT_SERIES)
      .param("id", series.id.value)
      .param("name", series.name)
      .query { rs, _ -> seriesOf(rs) }
      .single()

  override fun findById(seriesId: SeriesId): Series? =
    jdbcClient
      .sql(FIND_SERIES_BY_ID)
      .param("id", seriesId.value)
      .query { rs, _ -> seriesOf(rs) }
      .optional()
      .orElse(null)

  override fun findByIds(seriesIds: List<SeriesId>): List<Series> =
    if (seriesIds.isEmpty()) {
      emptyList()
    } else {
      jdbcClient
        .sql(FIND_SERIES_BY_IDS)
        .param("ids", seriesIds.map { it.value })
        .query { rs, _ -> seriesOf(rs) }
        .list()
    }

  private fun seriesOf(rs: ResultSet): Series =
    Series(SeriesId(rs.getObject("id", UUID::class.java)), rs.getString("name"))
}
