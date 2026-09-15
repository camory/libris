package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.AuthorRole.ARTIST
import fr.amory.libris.domain.AuthorRole.TRANSLATOR
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.fixture.A_SOURCE_EDITION
import fr.amory.libris.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MergeTest {
    @Test
    fun `one edition alone merges to itself`() {
        // Given
        val edition = A_SOURCE_EDITION.copy(title = "Romance dawn", publisher = "Glénat")

        // When
        val merged = merge(listOf(edition))

        // Then
        merged shouldBe edition
    }

    @Test
    fun `two sources that both give every field answer the first one's values`() {
        // Given
        val first = A_SOURCE_EDITION.copy(
            title = "Romance dawn",
            subtitle = "à l'aube d'une grande aventure",
            authors = listOf(SourceAuthor("Eiichirō Oda", WRITER)),
            series = SourceSeries("One piece", 1),
            collection = "Shonen manga",
            publisher = "Glénat",
            publicationYear = 2013,
            language = "fr",
            pageCount = 203,
            summary = "Luffy prend la mer",
            coverUrl = "https://example.org/une-couverture.jpg",
        )
        val second = A_SOURCE_EDITION.copy(
            title = "One Piece - Édition originale Tome 01",
            subtitle = "un autre sous-titre",
            authors = listOf(SourceAuthor("Sylvain Chollet", TRANSLATOR)),
            series = SourceSeries("One Piece", 2),
            collection = "Shōnen",
            publisher = "Glénat Manga",
            publicationYear = 2003,
            language = "ja",
            pageCount = 207,
            summary = "un autre résumé",
            coverUrl = "https://example.org/une-autre-couverture.jpg",
        )

        // When
        val merged = merge(listOf(first, second))

        // Then
        merged shouldBe first
    }

    @Test
    fun `a field the first source leaves empty is the next source's`() {
        // Given
        val first = A_SOURCE_EDITION.copy(title = "Romance dawn")
        val second = A_SOURCE_EDITION.copy(
            title = "One Piece - Édition originale Tome 01",
            subtitle = "à l'aube d'une grande aventure",
            authors = listOf(SourceAuthor("Eiichirō Oda", WRITER)),
            series = SourceSeries("One piece", 1),
            collection = "Shonen manga",
            publisher = "Glénat",
            publicationYear = 2013,
            language = "fr",
            pageCount = 203,
            summary = "Luffy prend la mer",
            coverUrl = "https://example.org/une-couverture.jpg",
        )

        // When
        val merged = merge(listOf(first, second))

        // Then
        merged shouldBe second.copy(title = "Romance dawn")
    }

    @Test
    fun `the authors come whole from the first source that has any`() {
        // Given
        val first = A_SOURCE_EDITION.copy(authors = emptyList())
        val second = A_SOURCE_EDITION.copy(authors = listOf(SourceAuthor("Eiichirō Oda", WRITER)))
        val third = A_SOURCE_EDITION.copy(
            authors = listOf(SourceAuthor("Eiichirō Oda", ARTIST), SourceAuthor("Sylvain Chollet", TRANSLATOR)),
        )

        // When
        val merged = merge(listOf(first, second, third))

        // Then
        merged.authors shouldBe listOf(SourceAuthor("Eiichirō Oda", WRITER))
    }

    @Test
    fun `a series is taken whole, the next source's volume number ignored`() {
        // Given
        val first = A_SOURCE_EDITION.copy(series = SourceSeries("One piece", null))
        val second = A_SOURCE_EDITION.copy(series = SourceSeries("One Piece", 1))

        // When
        val merged = merge(listOf(first, second))

        // Then
        merged.series shouldBe SourceSeries("One piece", null)
    }

    @Test
    fun `the cover comes from the first source that has one`() {
        // Given
        val first = A_SOURCE_EDITION.copy(coverUrl = null)
        val second = A_SOURCE_EDITION.copy(coverUrl = "https://example.org/une-couverture.jpg")
        val third = A_SOURCE_EDITION.copy(coverUrl = "https://example.org/une-autre-couverture.jpg")

        // When
        val merged = merge(listOf(first, second, third))

        // Then
        merged.coverUrl shouldBe "https://example.org/une-couverture.jpg"
    }

    @Test
    fun `the ISBN is the one every source answered`() {
        // Given
        val editions = listOf(
            A_SOURCE_EDITION.copy(isbn = isbnOf("9782380751673"), title = "Space Wars - Chapitre 1"),
            A_SOURCE_EDITION.copy(isbn = isbnOf("9782380751673"), publisher = "KENNES EDITIONS"),
        )

        // When
        val merged = merge(editions)

        // Then
        merged.isbn shouldBe isbnOf("9782380751673")
    }
}
