package fr.amory.libris.bibliography.domain.cover

class Cover private constructor(val format: CoverFormat, val bytes: ByteArray) {
    companion object {
        private val JPEG_START = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

        fun of(bytes: ByteArray): Cover? =
            if (bytes.startsWith(JPEG_START)) Cover(CoverFormat.JPEG, bytes) else null

        private fun ByteArray.startsWith(start: ByteArray): Boolean =
            size >= start.size && start.indices.all { this[it] == start[it] }
    }
}
