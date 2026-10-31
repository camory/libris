package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.lookup.EditionLookup
import fr.amory.libris.bibliography.domain.lookup.EditionSource.BNF
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Known
import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.springframework.stereotype.Component
import java.util.UUID

private const val SELECT_EDITIONS_WITHOUT_SUMMARY =
  """
    SELECT edition.id, edition.isbn13
    FROM edition
    WHERE edition.isbn13 IS NOT NULL
      AND edition.summary IS NULL
    """

private const val UPDATE_SUMMARY =
  "UPDATE edition SET summary = :summary WHERE edition.id = :id"

@Component
@Suppress("ClassNaming")
class V013__summary_for_stored_editions(private val editionLookups: List<EditionLookup>) : BaseJavaMigration() {
  private val logger = KotlinLogging.logger {}

  override fun migrate(context: Context) {
    val jdbcClient = JdbcClient.create(SingleConnectionDataSource(context.connection, true))
    val bnf = editionLookups.single { it.source == BNF }
    jdbcClient
      .sql(SELECT_EDITIONS_WITHOUT_SUMMARY)
      .query { rs, _ -> StoredEdition(rs.getObject("id", UUID::class.java), rs.getString("isbn13")) }
      .list()
      .forEach { edition ->
        Isbn
          .of(edition.isbn13)
          ?.let { summaryFor(it, bnf) }
          ?.let { jdbcClient.sql(UPDATE_SUMMARY).param("summary", it).param("id", edition.id).update() }
      }
  }

  @Suppress("TooGenericExceptionCaught")
  private fun summaryFor(isbn: Isbn, bnf: EditionLookup): String? =
    try {
      (bnf.answerFor(isbn) as? Known)?.preview?.summary
    } catch (e: Exception) {
      logger.warn(e) { "Asking the summary of ${isbn.digits} failed" }
      null
    }

  private data class StoredEdition(val id: UUID, val isbn13: String)
}
