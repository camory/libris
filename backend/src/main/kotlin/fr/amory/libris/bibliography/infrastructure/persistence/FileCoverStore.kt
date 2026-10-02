package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverStore
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.io.path.createTempFile
import kotlin.io.path.isRegularFile
import kotlin.io.path.moveTo
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes

class FileCoverStore(private val dir: Path) : CoverStore {
  override fun read(name: CoverName): Cover? {
    val picture = dir.resolve(name.value)
    val mediaType = dir.resolve("${name.value}.type")
    return if (picture.isRegularFile() && mediaType.isRegularFile()) {
      Cover.of(mediaType.readText(), picture.readBytes())
    } else {
      null
    }
  }

  override fun write(cover: Cover) {
    place(cover.name.value, cover.bytes)
    place("${cover.name.value}.type", cover.mediaType.encodeToByteArray())
  }

  private fun place(fileName: String, bytes: ByteArray) {
    val draft = createTempFile(dir, fileName, ".draft")
    draft.writeBytes(bytes)
    draft.moveTo(dir.resolve(fileName), ATOMIC_MOVE, REPLACE_EXISTING)
  }
}
