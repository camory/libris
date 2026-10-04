package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.fixture.pictureOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class CoverTest {
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

  @Test
  fun `a cover is named by the SHA-256 of its bytes`() {
    // Given / When / Then
    Cover.of("image/png", "test".encodeToByteArray())?.name shouldBe
      CoverName("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08")
  }

  @Test
  fun `a hash that begins with zeros keeps all its digits`() {
    // Given / When / Then
    Cover.of("image/png", "cover 59".encodeToByteArray())?.name shouldBe
      CoverName("005cbc1fbb9398468b932b0a7417723d1134b4cad70831f5587e236583f1a458")
  }

  @Test
  fun `a picture taller than 600 is scaled to 600 tall, as JPEG`() {
    // Given
    val cover = checkNotNull(Cover.of("image/png", pictureOf("png", 400, 1000, TYPE_INT_ARGB)))

    // When
    val normalised = cover.normalised()

    // Then
    normalised.mediaType shouldBe "image/jpeg"
    val picture = ImageIO.read(ByteArrayInputStream(normalised.bytes))
    picture.width shouldBe 240
    picture.height shouldBe 600
  }

  @Test
  fun `a picture 600 tall is kept as fetched`() {
    // Given
    val bytes = pictureOf("png", 400, 600)
    val cover = checkNotNull(Cover.of("image/png", bytes))

    // When
    val normalised = cover.normalised()

    // Then
    normalised.mediaType shouldBe "image/png"
    normalised.bytes shouldBe bytes
  }

  @Test
  fun `a picture the JDK reads no image from is kept as fetched`() {
    // Given
    val bytes = recordedBytes("covers/small.webp")
    val cover = checkNotNull(Cover.of("image/webp", bytes))

    // When
    val normalised = cover.normalised()

    // Then
    normalised.mediaType shouldBe "image/webp"
    normalised.bytes shouldBe bytes
  }

  @Test
  fun `a picture the JDK fails to read is kept as fetched`() {
    // Given
    val bytes = recordedBytes("covers/tall.jpg").copyOf(200)
    val cover = checkNotNull(Cover.of("image/jpeg", bytes))

    // When
    val normalised = cover.normalised()

    // Then
    normalised.mediaType shouldBe "image/jpeg"
    normalised.bytes shouldBe bytes
  }
}
