package fr.amory.libris.bibliography.domain.cover

class Cover(val mediaType: String, val bytes: ByteArray) {
  init {
    require(isImage(mediaType)) { "a cover's media type is an image" }
  }

  companion object {
    fun of(mediaType: String, bytes: ByteArray): Cover? =
      mediaType.takeIf { isImage(it) }?.let { Cover(it, bytes) }

    private fun isImage(mediaType: String): Boolean =
      mediaType.startsWith("image/")
  }
}
