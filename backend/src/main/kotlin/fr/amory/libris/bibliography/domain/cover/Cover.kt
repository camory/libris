package fr.amory.libris.bibliography.domain.cover

import java.security.MessageDigest
import java.util.HexFormat

class Cover private constructor(val mediaType: String, val bytes: ByteArray) {
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
