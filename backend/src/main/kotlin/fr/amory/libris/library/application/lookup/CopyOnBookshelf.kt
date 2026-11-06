package fr.amory.libris.library.application.lookup

import fr.amory.libris.library.application.ReaderBookshelf
import fr.amory.libris.library.domain.copy.CopyId

data class CopyOnBookshelf(
  val copyId: CopyId,
  val bookshelf: ReaderBookshelf)
