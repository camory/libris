package fr.amory.libris.library.application.lookup

import fr.amory.libris.library.application.ReaderBookshelf
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId

data class CopyOnBookshelf(
  val copyId: CopyId,
  val bookshelf: ReaderBookshelf) {
  companion object {
    fun of(copy: Copy, bookshelf: ReaderBookshelf): CopyOnBookshelf =
      CopyOnBookshelf(copy.id, bookshelf)
  }
}
