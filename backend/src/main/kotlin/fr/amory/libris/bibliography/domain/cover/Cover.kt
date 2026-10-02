package fr.amory.libris.bibliography.domain.cover

import java.security.MessageDigest
import java.util.HexFormat

class Cover(val mediaType: String, val bytes: ByteArray) {
  init {
    require(isImage(mediaType)) { "a cover's media type is an image" }
  }

  val name: CoverName
    get() =
      CoverName(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)))

  companion object {
    fun of(mediaType: String, bytes: ByteArray): Cover? =
      mediaType.takeIf { isImage(it) }?.let { Cover(it, bytes) }

    private fun isImage(mediaType: String): Boolean =
      mediaType.startsWith("image/")
  }
}
