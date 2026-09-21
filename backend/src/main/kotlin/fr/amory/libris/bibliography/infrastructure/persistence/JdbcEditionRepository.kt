package fr.amory.libris.bibliography.infrastructure.persistence

import com.fasterxml.uuid.Generators
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_EDITION =
    """
    insert into edition (id, isbn13, kind, title, subtitle, series_id, volume_number, collection,
                         publisher, publication_year, language, page_count, summary, cover_url)
    values (:id, :isbn13, :kind, :title, :subtitle, :seriesId, :volumeNumber, :collection,
            :publisher, :publicationYear, :language, :pageCount, :summary, :coverUrl)
    """

private const val INSERT_SERIES =
    """
    insert into series (id, name) values (:id, :name)
    on conflict (lower(name)) do update set name = series.name
    returning id
    """

private const val INSERT_AUTHOR =
    """
    insert into author (id, name) values (:id, :name)
    on conflict (lower(name)) do update set name = author.name
    returning id
    """

private const val INSERT_CONTRIBUTION =
    "insert into contribution (edition_id, author_id, role, position) " +
        "values (:editionId, :authorId, :role, :position)"

private const val FIND_EDITION_BY_ISBN =
    """
    select edition.id, edition.isbn13, edition.kind, edition.title, edition.subtitle,
           edition.volume_number, edition.collection, edition.publisher, edition.publication_year,
           edition.language, edition.page_count, edition.summary, edition.cover_url,
           series.name as series_name, author.name as author_name, contribution.role
    from edition
    left join series on series.id = edition.series_id
    left join contribution on contribution.edition_id = edition.id
    left join author on author.id = contribution.author_id
    where edition.isbn13 = :isbn13
    order by contribution.position
    """

private class EditionRow(
    val edition: Edition,
    val contribution: Contribution?,
)

@Repository
class JdbcEditionRepository(private val jdbcClient: JdbcClient) : EditionRepository {
    private val uuids = Generators.timeBasedEpochGenerator()

    override fun insert(edition: Edition) {
        jdbcClient
            .sql(INSERT_EDITION)
            .param("id", edition.id.value)
            .param("isbn13", edition.isbn?.digits)
            .param("kind", edition.kind.name)
            .param("title", edition.title)
            .param("subtitle", edition.subtitle)
            .param("seriesId", edition.series?.let { named(INSERT_SERIES, it.name) })
            .param("volumeNumber", edition.series?.volumeNumber)
            .param("collection", edition.collection)
            .param("publisher", edition.publisher)
            .param("publicationYear", edition.publicationYear)
            .param("language", edition.language)
            .param("pageCount", edition.pageCount)
            .param("summary", edition.summary)
            .param("coverUrl", edition.coverUrl)
            .update()
        edition.contributions.forEachIndexed { position, contribution ->
            jdbcClient
                .sql(INSERT_CONTRIBUTION)
                .param("editionId", edition.id.value)
                .param("authorId", named(INSERT_AUTHOR, contribution.name))
                .param("role", contribution.role.name)
                .param("position", position)
                .update()
        }
    }

    override fun findByIsbn(isbn: Isbn): Edition? =
        jdbcClient
            .sql(FIND_EDITION_BY_ISBN)
            .param("isbn13", isbn.digits)
            .query { rs, _ ->
                EditionRow(
                    edition = Edition(
                        id = EditionId(rs.getObject("id", UUID::class.java)),
                        isbn = rs.getString("isbn13")?.let { Isbn.of(it) },
                        kind = Kind.valueOf(rs.getString("kind")),
                        title = rs.getString("title"),
                        subtitle = rs.getString("subtitle"),
                        contributions = emptyList(),
                        series = SeriesEntry.of(
                            rs.getString("series_name"),
                            rs.getObject("volume_number", Int::class.javaObjectType),
                        ),
                        collection = rs.getString("collection"),
                        publisher = rs.getString("publisher"),
                        publicationYear = rs.getObject("publication_year", Int::class.javaObjectType),
                        language = rs.getString("language"),
                        pageCount = rs.getObject("page_count", Int::class.javaObjectType),
                        summary = rs.getString("summary"),
                        coverUrl = rs.getString("cover_url"),
                    ),
                    contribution = rs.getString("author_name")?.let { name ->
                        Contribution.of(name, ContributionRole.valueOf(rs.getString("role")))
                    },
                )
            }
            .list()
            .takeIf { it.isNotEmpty() }
            ?.let { rows -> rows.first().edition.copy(contributions = rows.mapNotNull { it.contribution }) }

    private fun named(sql: String, name: String): UUID =
        jdbcClient
            .sql(sql)
            .param("id", uuids.generate())
            .param("name", name)
            .query(UUID::class.java)
            .single()
}
