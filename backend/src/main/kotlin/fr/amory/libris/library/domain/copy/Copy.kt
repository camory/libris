package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import java.time.Instant

data class Copy(
  val id: CopyId,
  val editionId: EditionId,
  val bookshelfId: BookshelfId,
  val addedBy: ReaderId,
  val addedAt: Instant)
