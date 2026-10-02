package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverStore
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

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
    if (read(cover.name) != null) {
      return
    }
    dir.resolve(cover.name.value).writeBytes(cover.bytes)
    dir.resolve("${cover.name.value}.type").writeText(cover.mediaType)
  }
}
