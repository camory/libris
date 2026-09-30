package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverTest {
    @Test
    fun `bytes starting FF D8 FF are a JPEG cover`() {
        // Given
        val bytes = recordedBytes("covers/tall.jpg")

        // When
        val cover = Cover.of(bytes)

        // Then
        cover?.format shouldBe CoverFormat.JPEG
        cover?.bytes shouldBe bytes
    }
}
