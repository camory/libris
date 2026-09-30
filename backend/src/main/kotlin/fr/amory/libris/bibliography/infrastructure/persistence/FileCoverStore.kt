package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverStore
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes

class FileCoverStore(private val dir: Path) : CoverStore {
    override fun read(name: CoverName): Cover? =
        dir.resolve(name.value).takeIf { it.isRegularFile() }?.let { Cover.of(it.readBytes()) }
}
