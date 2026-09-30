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

    @Test
    fun `bytes with RIFF at zero and WEBP at eight are a WebP cover`() {
        // Given
        val bytes = recordedBytes("covers/small.webp")

        // When
        val cover = Cover.of(bytes)

        // Then
        cover?.format shouldBe CoverFormat.WEBP
        cover?.bytes shouldBe bytes
    }

    @Test
    fun `bytes of neither format are no cover`() {
        // Given / When / Then
        Cover.of("Luffy rêve de devenir le roi des pirates.".toByteArray()) shouldBe null
        Cover.of("RIFF".toByteArray()) shouldBe null
    }
}
