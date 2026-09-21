package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient

private const val ONE_PIECE = "9782723488525"
private const val ONE_PIECE_TOME_TWO = "9782723489898"

@JdbcSliceTest
@Import(JdbcEditionRepository::class)
class JdbcEditionRepositoryTest @Autowired constructor(
    private val editions: JdbcEditionRepository,
    private val jdbcClient: JdbcClient,
) {
    @Test
    fun `an inserted edition is read back whole by its ISBN-13`() {
        // Given
        val edition = onePieceTomeOne()

        // When
        editions.insert(edition)

        // Then
        editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition
    }

    @Test
    fun `an edition with nothing but a kind and a title is read back whole`() {
        // Given
        val edition = bare(onePieceTomeOne())

        // When
        editions.insert(edition)

        // Then
        editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe edition
    }

    @Test
    fun `a series and an author named again in another capitalisation are not doubled`() {
        // Given
        val first = onePieceTomeOne()
        val second = first.copy(
            id = EditionId.new(),
            isbn = isbnOf(ONE_PIECE_TOME_TWO),
            title = "Aux prises avec Baggy et ses hommes",
            contributions = listOf(
                Contribution("EIICHIRO ODA", WRITER),
                Contribution("akiko indei", TRANSLATOR),
            ),
            series = SeriesEntry("ONE PIECE", 2),
        )
        editions.insert(first)

        // When
        editions.insert(second)

        // Then
        rowsOf("series") shouldBe 1
        rowsOf("author") shouldBe 2
        editions.findByIsbn(isbnOf(ONE_PIECE_TOME_TWO)) shouldBe second.copy(
            contributions = first.contributions,
            series = SeriesEntry("One piece", 2),
        )
    }

    @Test
    fun `an edition without an ISBN is found by no ISBN-13, and a second one stands beside it`() {
        // Given
        val first = onePieceTomeOne().copy(isbn = null)
        editions.insert(first)

        // When
        editions.insert(first.copy(id = EditionId.new(), title = "Aux prises avec Baggy et ses hommes"))

        // Then
        editions.findByIsbn(isbnOf(ONE_PIECE)) shouldBe null
        rowsOf("edition") shouldBe 2
    }

    private fun rowsOf(table: String): Int =
        jdbcClient.sql("select count(*) from $table").query(Int::class.java).single()

    private fun bare(edition: Edition): Edition = edition.copy(
        subtitle = null,
        contributions = emptyList(),
        series = null,
        collection = null,
        publisher = null,
        publicationYear = null,
        language = null,
        pageCount = null,
        summary = null,
        coverUrl = null,
    )

    private fun onePieceTomeOne(): Edition = Edition(
        id = EditionId.new(),
        isbn = isbnOf(ONE_PIECE),
        kind = MANGA,
        title = "Romance dawn",
        subtitle = "À l'aube d'une grande aventure",
        contributions = listOf(
            Contribution("Eiichiro Oda", WRITER),
            Contribution("Akiko Indei", TRANSLATOR),
        ),
        series = SeriesEntry("One piece", 1),
        collection = "Shonen manga",
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        pageCount = 208,
        summary = "Luffy prend la mer pour devenir le roi des pirates.",
        coverUrl = "https://covers.libris.test/9782723488525.jpg",
    )
}
