package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.AuthorRole.ARTIST
import fr.amory.libris.domain.AuthorRole.TRANSLATOR
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.fixture.isbn13Of
import fr.amory.libris.fixture.sourceEdition
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MergeTest {
    @Test
    fun `one edition alone merges to itself, with the cover of the asked ISBN`() {
        // Given
        val edition = sourceEdition(title = "Romance dawn", publisher = "Glénat")

        // When
        val merged = merge(isbn13Of("9782723488525"), listOf(edition))

        // Then
        merged shouldBe edition.copy(coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg")
    }

    @Test
    fun `two editions that both give every field answer the first one's values`() {
        // Given
        val first = sourceEdition(
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
        val second = sourceEdition(
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
        val merged = merge(isbn13Of("9782723488525"), listOf(first, second))

        // Then
        merged shouldBe sourceEdition(
            isbn13 = isbn13Of("9782723488525"),
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
            coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
        )
    }

    @Test
    fun `a field the first edition leaves empty is the next edition's`() {
        // Given
        val first = sourceEdition(title = "Romance dawn")
        val second = sourceEdition(
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
        val merged = merge(isbn13Of("9782723488525"), listOf(first, second))

        // Then
        merged shouldBe sourceEdition(
            isbn13 = isbn13Of("9782723488525"),
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
            coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
        )
    }

    @Test
    fun `authors are taken whole from the first edition that gives any`() {
        // Given
        val first = sourceEdition(authors = listOf(SourceAuthor("Eiichirō Oda", WRITER)))
        val second = sourceEdition(
            authors = listOf(SourceAuthor("Eiichirō Oda", ARTIST), SourceAuthor("Sylvain Chollet", TRANSLATOR)),
        )

        // When
        val merged = merge(isbn13Of("9782723488525"), listOf(first, second))

        // Then
        merged.authors shouldBe listOf(SourceAuthor("Eiichirō Oda", WRITER))
    }

    @Test
    fun `a series is taken whole, the next edition's volume number ignored`() {
        // Given
        val first = sourceEdition(series = SourceSeries("One piece", null))
        val second = sourceEdition(series = SourceSeries("One Piece", 1))

        // When
        val merged = merge(isbn13Of("9782723488525"), listOf(first, second))

        // Then
        merged.series shouldBe SourceSeries("One piece", null)
    }
}
