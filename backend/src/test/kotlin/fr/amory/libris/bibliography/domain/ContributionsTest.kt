package fr.amory.libris.bibliography.domain

import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ContributionsTest {
    @Test
    fun `the contributions are ordered by role, then by name`() {
        // Given
        val given = listOf(
            Contribution("Sylvain Chollet", TRANSLATOR),
            Contribution("Jérémy", ARTIST),
            Contribution("Théo", WRITER),
            Contribution("Jean Dufaux", WRITER),
        )

        // When
        val contributions = Contributions.of(given)

        // Then
        contributions.all shouldBe listOf(
            Contribution("Jean Dufaux", WRITER),
            Contribution("Théo", WRITER),
            Contribution("Jérémy", ARTIST),
            Contribution("Sylvain Chollet", TRANSLATOR),
        )
    }

    @Test
    fun `an author named twice in one role is kept once, under the first spelling`() {
        // Given
        val given = listOf(Contribution("Eiichirō Oda", WRITER), Contribution("EIICHIRŌ ODA", WRITER))

        // When
        val contributions = Contributions.of(given)

        // Then
        contributions.all shouldBe listOf(Contribution("Eiichirō Oda", WRITER))
    }

    @Test
    fun `an author holds several roles`() {
        // Given
        val given = listOf(Contribution("Eiichirō Oda", ARTIST), Contribution("Eiichirō Oda", WRITER))

        // When
        val contributions = Contributions.of(given)

        // Then
        contributions.all shouldBe listOf(Contribution("Eiichirō Oda", WRITER), Contribution("Eiichirō Oda", ARTIST))
    }
}
