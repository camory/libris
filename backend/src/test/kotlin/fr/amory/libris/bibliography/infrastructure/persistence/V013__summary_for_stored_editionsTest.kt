package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.lookup.EditionLookup
import fr.amory.libris.bibliography.domain.lookup.EditionSource.BNF
import fr.amory.libris.bibliography.domain.lookup.EditionSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Known
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import fr.amory.libris.bibliography.fixture.LookupAnswering
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.matchers.shouldBe
import org.flywaydb.core.api.configuration.Configuration
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.datasource.DataSourceUtils
import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

@JdbcSliceTest
@Suppress("ClassNaming")
class V013__summary_for_stored_editionsTest @Autowired constructor(
  private val dataSource: DataSource,
  private val jdbcClient: JdbcClient) {
  @Test
  fun `an edition with an ISBN and no summary gains the summary the BnF gives`() {
    // Given
    val montTombe = editionStored(MONT_TOMBE)
    val bnf = LookupAnswering(Known(A_PREVIEW.copy(summary = "Un mont, une tombe."), null))

    // When
    migrationOver(bnf)

    // Then
    summaryOf(montTombe) shouldBe "Un mont, une tombe."
  }

  @Test
  fun `an edition with a summary is not asked`() {
    // Given
    val montTombe = editionStored(MONT_TOMBE, summary = "Le résumé déjà là.")
    val bnf = LookupAnswering(Known(A_PREVIEW.copy(summary = "Un mont, une tombe."), null))

    // When
    migrationOver(bnf)

    // Then
    summaryOf(montTombe) shouldBe "Le résumé déjà là."
    bnf.asked shouldBe emptyList()
  }

  @Test
  fun `Open Library is not asked`() {
    // Given
    val montTombe = editionStored(MONT_TOMBE)
    val openLibrary = LookupAnswering(Known(A_PREVIEW.copy(summary = "Selon Open Library."), null), OPEN_LIBRARY)
    val bnf = LookupAnswering(Known(A_PREVIEW.copy(summary = "Selon la BnF."), null), BNF)

    // When
    migrationOver(openLibrary, bnf)

    // Then
    summaryOf(montTombe) shouldBe "Selon la BnF."
    openLibrary.asked shouldBe emptyList()
  }

  @Test
  fun `the BnF throwing for the first of two editions, the second gains its summary`() {
    // Given
    val montTombe = editionStored(MONT_TOMBE)
    val larousse = editionStored(LAROUSSE_DU_PAIN)
    val bnf = LookupThrowingFor(isbnOf(MONT_TOMBE), Known(A_PREVIEW.copy(summary = "Du pain."), null))

    // When
    migrationOver(bnf)

    // Then
    summaryOf(montTombe) shouldBe null
    summaryOf(larousse) shouldBe "Du pain."
  }

  private fun migrationOver(vararg lookups: EditionLookup) {
    val context = MigrationContext(DataSourceUtils.getConnection(dataSource))
    V013__summary_for_stored_editions(lookups.toList()).migrate(context)
  }

  private fun editionStored(isbn13: String?, summary: String? = null): UUID =
    UUID.randomUUID().also { id ->
      jdbcClient
        .sql("INSERT INTO edition (id, isbn13, kind, title, summary) VALUES (:id, :isbn13, 'BOOK', 'Stocké', :summary)")
        .param("id", id)
        .param("isbn13", isbn13)
        .param("summary", summary)
        .update()
    }

  private fun summaryOf(edition: UUID): String? =
    jdbcClient
      .sql("SELECT summary FROM edition WHERE id = :id")
      .param("id", edition)
      .query { rs, _ -> rs.getString("summary") }
      .list()
      .single()

  private class MigrationContext(private val connection: Connection) : Context {
    override fun getConfiguration(): Configuration = FluentConfiguration()

    override fun getConnection(): Connection = connection
  }

  private class LookupThrowingFor(private val failing: Isbn, private val answer: EditionSourceAnswer) : EditionLookup {
    override val source = BNF

    override fun answerFor(isbn: Isbn): EditionSourceAnswer {
      check(isbn != failing) { "The record of ${isbn.digits} cannot be read" }
      return answer
    }
  }
}

private const val MONT_TOMBE = "9782344008980"
private const val LAROUSSE_DU_PAIN = "9782035973689"
