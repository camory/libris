package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.cover.Cover
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun coverOf(mediaType: String, bytes: ByteArray): Cover =
  checkNotNull(Cover.of(mediaType, bytes))

fun pictureOf(format: String, width: Int, height: Int, type: Int = TYPE_INT_RGB): ByteArray {
  val bytes = ByteArrayOutputStream()
  check(ImageIO.write(BufferedImage(width, height, type), format, bytes))
  return bytes.toByteArray()
}

fun jpegClaiming(width: Int, height: Int): ByteArray {
  val jpeg = pictureOf("jpeg", 1, 1)
  val frame = (0 until jpeg.size - 1).first { jpeg[it] == 0xFF.toByte() && jpeg[it + 1] == 0xC0.toByte() }
  jpeg.writeShort(frame + 5, height)
  jpeg.writeShort(frame + 7, width)
  return jpeg
}

private fun ByteArray.writeShort(index: Int, value: Int) {
  this[index] = (value shr 8).toByte()
  this[index + 1] = value.toByte()
}
