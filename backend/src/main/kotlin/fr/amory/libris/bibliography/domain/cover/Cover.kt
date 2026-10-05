package fr.amory.libris.bibliography.domain.cover

import java.awt.RenderingHints.KEY_INTERPOLATION
import java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.HexFormat
import javax.imageio.IIOException
import javax.imageio.ImageIO

class Cover private constructor(val mediaType: String, val bytes: ByteArray) {
  val name: CoverName
    get() =
      CoverName(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)))

  fun normalised(): Cover =
    picture()
      ?.takeIf { it.height > MAX_HEIGHT }
      ?.let { Cover(JPEG, jpegOf(scaled(it))) }
      ?: this

  private fun picture(): BufferedImage? =
    try {
      ImageIO.read(ByteArrayInputStream(bytes))
    } catch (_: IIOException) {
      null
    }

  private fun scaled(picture: BufferedImage): BufferedImage {
    val width = (picture.width * MAX_HEIGHT / picture.height).coerceAtLeast(1)
    val scaled = BufferedImage(width, MAX_HEIGHT, TYPE_INT_RGB)
    val graphics = scaled.createGraphics()
    graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC)
    graphics.drawImage(picture, 0, 0, scaled.width, scaled.height, null)
    graphics.dispose()
    return scaled
  }

  private fun jpegOf(picture: BufferedImage): ByteArray {
    val jpeg = ByteArrayOutputStream()
    ImageIO.write(picture, "jpeg", jpeg)
    return jpeg.toByteArray()
  }

  companion object {
    private const val MAX_HEIGHT = 600
    private const val JPEG = "image/jpeg"

    fun of(mediaType: String, bytes: ByteArray): Cover? =
      mediaType.takeIf { isImage(it) }?.let { Cover(it, bytes) }

    private fun isImage(mediaType: String): Boolean =
      mediaType.startsWith("image/")
  }
}
