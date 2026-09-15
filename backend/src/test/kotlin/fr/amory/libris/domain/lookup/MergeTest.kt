package fr.amory.libris.domain.lookup

import fr.amory.libris.fixture.A_SOURCE_EDITION
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
}
