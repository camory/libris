package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverStore

class CoversInMemory : CoverStore {
  private val covers = mutableMapOf<CoverName, Cover>()

  val stored: List<Cover> get() = covers.values.toList()

  override fun read(name: CoverName): Cover? =
    covers[name]

  override fun write(cover: Cover) {
    covers.putIfAbsent(cover.name, cover)
  }
}
