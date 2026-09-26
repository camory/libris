package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.catalogue.Catalogue
import fr.amory.libris.library.domain.catalogue.CatalogueCopy
import fr.amory.libris.library.domain.catalogue.CatalogueEdition
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

private const val FIND_EDITIONS_HELD_BY_READER =
    """
    SELECT edition.id, edition.isbn13, edition.kind, edition.title, edition.subtitle,
           edition.volume_number, edition.collection, edition.publisher, edition.publication_year,
           edition.language, edition.page_count, edition.summary, edition.cover_url,
           series.name AS series_name, author.name AS author_name, contribution.role,
           copy.id AS copy_id, bookshelf.id AS bookshelf_id, bookshelf.name AS bookshelf_name
    FROM membership
    JOIN bookshelf ON bookshelf.id = membership.bookshelf_id
    JOIN copy ON copy.bookshelf_id = bookshelf.id
    JOIN edition ON edition.id = copy.edition_id
    LEFT JOIN series ON series.id = edition.series_id
    LEFT JOIN contribution ON contribution.edition_id = edition.id
    LEFT JOIN author ON author.id = contribution.author_id
    WHERE membership.reader_id = :readerId
    """

private class CatalogueRow(
    val edition: CatalogueEdition,
    val contribution: Contribution?,
    val copy: CatalogueCopy,
)

@Repository
class JdbcCatalogue(private val jdbcClient: JdbcClient) : Catalogue {
    override fun editionsHeldBy(readerId: ReaderId): List<CatalogueEdition> =
        jdbcClient
            .sql(FIND_EDITIONS_HELD_BY_READER)
            .param("readerId", readerId.value)
            .query { rs, _ -> rowOf(rs) }
            .list()
            .map { row ->
                row.edition.copy(
                    contributions = Contributions.of(listOfNotNull(row.contribution)),
                    copies = listOf(row.copy),
                )
            }

    private fun rowOf(rs: ResultSet): CatalogueRow = CatalogueRow(
        edition = CatalogueEdition(
            editionId = EditionId(rs.getObject("id", UUID::class.java)),
            isbn = rs.getString("isbn13")?.let { Isbn.of(it) },
            kind = Kind.valueOf(rs.getString("kind")),
            title = rs.getString("title"),
            subtitle = rs.getString("subtitle"),
            contributions = Contributions.of(emptyList()),
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
            copies = emptyList(),
        ),
        contribution = rs.getString("author_name")?.let { name ->
            Contribution.of(name, ContributionRole.valueOf(rs.getString("role")))
        },
        copy = CatalogueCopy(
            copyId = CopyId(rs.getObject("copy_id", UUID::class.java)),
            bookshelfId = BookshelfId(rs.getObject("bookshelf_id", UUID::class.java)),
            bookshelfName = rs.getString("bookshelf_name"),
        ),
    )
}
