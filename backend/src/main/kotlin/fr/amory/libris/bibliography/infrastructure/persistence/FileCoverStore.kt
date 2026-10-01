package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverStore
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes

class FileCoverStore(private val dir: Path) : CoverStore {
    override fun read(name: CoverName): Cover? {
        val bytes = dir.resolve(name.value).takeIf { it.isRegularFile() }?.readBytes() ?: return null
        return mediaTypeOf(bytes)?.let { Cover(it, bytes) }
    }

    private fun mediaTypeOf(bytes: ByteArray): String? = when {
        bytes.holdsAt(0, JPEG_START) -> "image/jpeg"
        bytes.holdsAt(0, RIFF) && bytes.holdsAt(WEBP_OFFSET, WEBP) -> "image/webp"
        else -> null
    }

    private fun ByteArray.holdsAt(offset: Int, part: ByteArray): Boolean =
        size >= offset + part.size && part.indices.all { this[offset + it] == part[it] }

    private companion object {
        val JPEG_START = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        val RIFF = "RIFF".toByteArray()
        val WEBP = "WEBP".toByteArray()
        const val WEBP_OFFSET = 8
    }
}
