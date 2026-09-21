package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Kind.MANGA
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditionTest {
    @Test
    fun `an author named twice in one role is refused, whatever the capitalisation`() {
        shouldThrow<IllegalArgumentException> {
            editionBy(Contribution("Eiichiro Oda", WRITER), Contribution("EIICHIRO ODA", WRITER))
        }
    }

    @Test
    fun `an author holds several roles`() {
        // Given
        val contributions = listOf(Contribution("Eiichiro Oda", WRITER), Contribution("Eiichiro Oda", ARTIST))

        // When
        val edition = editionBy(*contributions.toTypedArray())

        // Then
        edition.contributions shouldBe contributions
    }

    private fun editionBy(vararg contributions: Contribution): Edition = Edition(
        id = EditionId.new(),
        isbn = null,
        kind = MANGA,
        title = "Romance dawn",
        subtitle = null,
        contributions = contributions.toList(),
        series = null,
        collection = null,
        publisher = null,
        publicationYear = null,
        language = null,
        pageCount = null,
        summary = null,
        coverUrl = null,
    )
}
