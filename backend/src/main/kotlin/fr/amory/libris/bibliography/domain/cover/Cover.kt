package fr.amory.libris.bibliography.domain.cover

import java.awt.RenderingHints.KEY_INTERPOLATION
import java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.HexFormat
import javax.imageio.ImageIO

class Cover private constructor(val mediaType: String, val bytes: ByteArray) {
  val name: CoverName
    get() =
      CoverName(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)))

  fun normalised(): Cover {
    val picture = ImageIO.read(ByteArrayInputStream(bytes))
    return if (picture.height > MAX_HEIGHT) Cover(JPEG, jpegOf(scaled(picture))) else this
  }

  private fun scaled(picture: BufferedImage): BufferedImage {
    val scaled = BufferedImage(picture.width * MAX_HEIGHT / picture.height, MAX_HEIGHT, TYPE_INT_RGB)
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
