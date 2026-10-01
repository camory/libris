package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeBytes

private const val NAME = "3f7a9c0e5b2d4816a0c9e7f1b3d5a2c4e6f8091b2c3d4e5f60718293a4b5c6d7"

class FileCoverStoreTest {
    @TempDir
    lateinit var dir: Path

    @Test
    fun `the file named by the name is the cover, a JPEG by its first bytes`() {
        // Given
        val bytes = recordedBytes("covers/tall.jpg")
        dir.resolve(NAME).writeBytes(bytes)

        // When
        val cover = FileCoverStore(dir).read(CoverName(NAME))

        // Then
        cover?.mediaType shouldBe "image/jpeg"
        cover?.bytes shouldBe bytes
    }

    @Test
    fun `a file with RIFF at zero and WEBP at eight is a WebP cover`() {
        // Given
        val bytes = recordedBytes("covers/small.webp")
        dir.resolve(NAME).writeBytes(bytes)

        // When
        val cover = FileCoverStore(dir).read(CoverName(NAME))

        // Then
        cover?.mediaType shouldBe "image/webp"
        cover?.bytes shouldBe bytes
    }

    @Test
    fun `a file of neither format is no cover`() {
        // Given
        val store = FileCoverStore(dir)
        val text = "4b1d0f3a6c8e2a5d7f9b1c3e5a7092b4d6f8e0a1c3b5d7f9e1a2c4b6d8f0a3c5"
        val riffAlone = "5c2e1a4b7d9f3b6e8a0c2d4f6b81a3c5e7f9d1b2a4c6e8f0b2d3a5c7e9f1b4d6"
        dir.resolve(text).writeBytes("Luffy rêve de devenir le roi des pirates.".toByteArray())
        dir.resolve(riffAlone).writeBytes("RIFF".toByteArray())

        // When / Then
        store.read(CoverName(text)) shouldBe null
        store.read(CoverName(riffAlone)) shouldBe null
    }

    @Test
    fun `a name no file carries is no cover`() {
        // Given / When / Then
        FileCoverStore(dir).read(CoverName(NAME)) shouldBe null
    }
}
