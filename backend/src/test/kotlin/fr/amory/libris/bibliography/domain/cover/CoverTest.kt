package fr.amory.libris.bibliography.domain.cover

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverTest {
    @Test
    fun `a media type that is not an image is no cover's`() {
        // Given / When / Then
        shouldThrow<IllegalArgumentException> { Cover("text/html", byteArrayOf(1, 2, 3)) }
        shouldThrow<IllegalArgumentException> { Cover("jpeg", byteArrayOf(1, 2, 3)) }
    }

    @Test
    fun `a picture and the media type of an image are a cover`() {
        // Given
        val bytes = byteArrayOf(1, 2, 3)

        // When
        val cover = Cover.of("image/png", bytes)

        // Then
        cover?.mediaType shouldBe "image/png"
        cover?.bytes shouldBe bytes
    }

    @Test
    fun `a picture and a media type that is not an image are no cover`() {
        // Given / When / Then
        Cover.of("text/html", byteArrayOf(1, 2, 3)) shouldBe null
        Cover.of("jpeg", byteArrayOf(1, 2, 3)) shouldBe null
    }
}
