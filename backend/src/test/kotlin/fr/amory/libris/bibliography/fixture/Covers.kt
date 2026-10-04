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
