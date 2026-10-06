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
import javax.imageio.ImageReader
import javax.imageio.stream.ImageInputStream

class Cover private constructor(val mediaType: String, val bytes: ByteArray) {
  val name: CoverName
    get() =
      CoverName(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)))

  fun normalised(): Cover? =
    try {
      pictureWithinBounds()?.let { normalisedFrom(it) }
    } catch (_: IIOException) {
      null
    }

  private fun pictureWithinBounds(): BufferedImage? =
    ImageIO.createImageInputStream(ByteArrayInputStream(bytes)).use { input ->
      ImageIO
        .getImageReaders(input)
        .asSequence()
        .firstOrNull()
        ?.let { read(it, input) }
    }

  private fun read(reader: ImageReader, input: ImageInputStream): BufferedImage? =
    try {
      reader.setInput(input, true, true)
      reader
        .takeIf { it.getWidth(0) <= MAX_SIDE && it.getHeight(0) <= MAX_SIDE }
        ?.read(0)
    } finally {
      reader.dispose()
    }

  private fun normalisedFrom(picture: BufferedImage): Cover =
    picture
      .takeIf { it.height > MAX_HEIGHT }
      ?.let { Cover(JPEG, jpegOf(scaled(it))) }
      ?: this

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
    private const val MAX_SIDE = 5_000
    private const val JPEG = "image/jpeg"

    fun of(mediaType: String, bytes: ByteArray): Cover? =
      mediaType.takeIf { isImage(it) }?.let { Cover(it, bytes) }

    private fun isImage(mediaType: String): Boolean =
      mediaType.startsWith("image/")
  }
}
