package fr.amory.libris.bibliography.domain.cover

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class CoverTest {
    @Test
    fun `a media type that is not an image is no cover's`() {
        // Given / When / Then
        shouldThrow<IllegalArgumentException> { Cover("text/html", byteArrayOf(1, 2, 3)) }
        shouldThrow<IllegalArgumentException> { Cover("jpeg", byteArrayOf(1, 2, 3)) }
    }
}
