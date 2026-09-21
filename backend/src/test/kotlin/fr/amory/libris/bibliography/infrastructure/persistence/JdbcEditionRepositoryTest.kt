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

private const val ONE_PIECE = "9782723488525"

@JdbcSliceTest
@Import(JdbcEditionRepository::class)
class JdbcEditionRepositoryTest @Autowired constructor(
    private val editions: JdbcEditionRepository,
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
