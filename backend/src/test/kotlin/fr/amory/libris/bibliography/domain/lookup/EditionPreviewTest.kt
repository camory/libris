package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.fixture.A_PREVIEW
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditionPreviewTest {
    @Test
    fun `two previews that both give every field merge to the first one`() {
        // Given
        val first = A_PREVIEW.copy(
            title = "Romance dawn",
            subtitle = "à l'aube d'une grande aventure",
            contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
            series = SeriesEntry("One piece", 1),
            collection = "Shonen manga",
            publisher = "Glénat",
            publicationYear = 2013,
            language = "fr",
            pageCount = 203,
            summary = "Luffy prend la mer",
            coverUrl = "https://example.org/une-couverture.jpg",
        )
        val second = A_PREVIEW.copy(
            title = "One Piece - Édition originale Tome 01",
            subtitle = "un autre sous-titre",
            contributions = Contributions.of(listOf(Contribution("Sylvain Chollet", TRANSLATOR))),
            series = SeriesEntry("One Piece", 2),
            collection = "Shōnen",
            publisher = "Glénat Manga",
            publicationYear = 2003,
            language = "ja",
            pageCount = 207,
            summary = "un autre résumé",
            coverUrl = "https://example.org/une-autre-couverture.jpg",
        )

        // When
        val merged = first.merge(second)

        // Then
        merged shouldBe first
    }

    @Test
    fun `a field the first preview leaves empty is the other's`() {
        // Given
        val first = A_PREVIEW.copy(title = "Romance dawn")
        val second = A_PREVIEW.copy(
            title = "One Piece - Édition originale Tome 01",
            subtitle = "à l'aube d'une grande aventure",
            contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
            series = SeriesEntry("One piece", 1),
            collection = "Shonen manga",
            publisher = "Glénat",
            publicationYear = 2013,
            language = "fr",
            pageCount = 203,
            summary = "Luffy prend la mer",
            coverUrl = "https://example.org/une-couverture.jpg",
        )

        // When
        val merged = first.merge(second)

        // Then
        merged shouldBe second.copy(title = "Romance dawn")
    }

    @Test
    fun `the contributions come whole from the first preview that has any`() {
        // Given
        val first = A_PREVIEW.copy(contributions = Contributions.of(emptyList()))
        val second = A_PREVIEW.copy(contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))))
        val third = A_PREVIEW.copy(
            contributions = Contributions.of(
                listOf(
                    Contribution("Eiichirō Oda", ARTIST),
                    Contribution("Sylvain Chollet", TRANSLATOR),
                ),
            ),
        )

        // When
        val merged = first.merge(second).merge(third)

        // Then
        merged.contributions.toList() shouldBe listOf(Contribution("Eiichirō Oda", WRITER))
    }

    @Test
    fun `a series is taken whole, the other's volume number ignored`() {
        // Given
        val first = A_PREVIEW.copy(series = SeriesEntry("One piece", null))
        val second = A_PREVIEW.copy(series = SeriesEntry("One Piece", 1))

        // When
        val merged = first.merge(second)

        // Then
        merged.series shouldBe SeriesEntry("One piece", null)
    }

    @Test
    fun `the kind is the first preview's`() {
        // Given
        val first = A_PREVIEW.copy(kind = MANGA)
        val second = A_PREVIEW.copy(kind = BD)

        // When
        val merged = first.merge(second)

        // Then
        merged.kind shouldBe MANGA
    }

    @Test
    fun `the cover comes from the first preview that has one`() {
        // Given
        val first = A_PREVIEW.copy(coverUrl = null)
        val second = A_PREVIEW.copy(coverUrl = "https://example.org/une-couverture.jpg")
        val third = A_PREVIEW.copy(coverUrl = "https://example.org/une-autre-couverture.jpg")

        // When
        val merged = first.merge(second).merge(third)

        // Then
        merged.coverUrl shouldBe "https://example.org/une-couverture.jpg"
    }
}
