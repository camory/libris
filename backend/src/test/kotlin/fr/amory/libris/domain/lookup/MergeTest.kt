package fr.amory.libris.domain.lookup

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
}
