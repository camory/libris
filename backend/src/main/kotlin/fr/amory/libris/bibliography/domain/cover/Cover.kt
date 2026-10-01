package fr.amory.libris.bibliography.domain.cover

class Cover(val mediaType: String, val bytes: ByteArray) {
    init {
        require(mediaType.startsWith("image/")) { "a cover's media type is an image" }
    }

    companion object {
        fun of(mediaType: String, bytes: ByteArray): Cover? = Cover(mediaType, bytes)
    }
}
