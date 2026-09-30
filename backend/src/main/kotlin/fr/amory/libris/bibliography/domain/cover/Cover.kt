package fr.amory.libris.bibliography.domain.cover

class Cover private constructor(val format: CoverFormat, val bytes: ByteArray) {
    companion object {
        private val JPEG_START = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        private val RIFF = "RIFF".toByteArray()
        private val WEBP = "WEBP".toByteArray()
        private const val WEBP_OFFSET = 8

        fun of(bytes: ByteArray): Cover? = when {
            bytes.holdsAt(0, JPEG_START) -> Cover(CoverFormat.JPEG, bytes)
            bytes.holdsAt(0, RIFF) && bytes.holdsAt(WEBP_OFFSET, WEBP) -> Cover(CoverFormat.WEBP, bytes)
            else -> null
        }

        private fun ByteArray.holdsAt(offset: Int, part: ByteArray): Boolean =
            size >= offset + part.size && part.indices.all { this[offset + it] == part[it] }
    }
}
