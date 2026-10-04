package fr.amory.libris.bibliography.domain.cover

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
    val cover = checkNotNull(Cover.of("image/png", pictureOf(400, 1000)))

    // When
    val normalised = cover.normalised()

    // Then
    normalised.mediaType shouldBe "image/jpeg"
    val picture = ImageIO.read(ByteArrayInputStream(normalised.bytes))
    picture.width shouldBe 240
    picture.height shouldBe 600
  }

  private fun pictureOf(width: Int, height: Int): ByteArray {
    val bytes = ByteArrayOutputStream()
    ImageIO.write(BufferedImage(width, height, TYPE_INT_ARGB), "png", bytes)
    return bytes.toByteArray()
  }
}
