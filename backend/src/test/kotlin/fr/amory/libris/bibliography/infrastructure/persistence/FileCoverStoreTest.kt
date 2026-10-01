package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

private const val NAME = "3f7a9c0e5b2d4816a0c9e7f1b3d5a2c4e6f8091b2c3d4e5f60718293a4b5c6d7"

class FileCoverStoreTest {
    @TempDir
    lateinit var dir: Path

    @Test
    fun `the file named by the name is the cover, its media type the one saved beside it`() {
        // Given
        val bytes = recordedBytes("covers/tall.jpg")
        dir.resolve(NAME).writeBytes(bytes)
        dir.resolve("$NAME.type").writeText("image/jpeg")

        // When
        val cover = FileCoverStore(dir).read(CoverName(NAME))

        // Then
        cover?.mediaType shouldBe "image/jpeg"
        cover?.bytes shouldBe bytes
    }

    @Test
    fun `a WebP saved as a WebP is a WebP cover`() {
        // Given
        val bytes = recordedBytes("covers/small.webp")
        dir.resolve(NAME).writeBytes(bytes)
        dir.resolve("$NAME.type").writeText("image/webp")

        // When
        val cover = FileCoverStore(dir).read(CoverName(NAME))

        // Then
        cover?.mediaType shouldBe "image/webp"
        cover?.bytes shouldBe bytes
    }

    @Test
    fun `a file with no media type saved beside it is no cover`() {
        // Given
        dir.resolve(NAME).writeBytes(recordedBytes("covers/tall.jpg"))

        // When / Then
        FileCoverStore(dir).read(CoverName(NAME)) shouldBe null
    }

    @Test
    fun `a name no file carries is no cover`() {
        // Given / When / Then
        FileCoverStore(dir).read(CoverName(NAME)) shouldBe null
    }
}
