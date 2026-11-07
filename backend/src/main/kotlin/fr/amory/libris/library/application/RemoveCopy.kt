package fr.amory.libris.library.application

import fr.amory.libris.library.application.RemoveCopyResult.NoSuchCopy
import fr.amory.libris.library.application.RemoveCopyResult.Removed
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class RemoveCopy(private val copies: CopyRepository, private val bookshelves: BookshelfRepository) {
  operator fun invoke(readerId: ReaderId, copyId: CopyId): RemoveCopyResult {
    val copy = copies
      .findById(copyId)
      ?.takeIf { bookshelves.findById(it.bookshelfId)?.isOwnedBy(readerId) == true }
      ?: return NoSuchCopy
    copies.delete(copy.id)
    return Removed
  }
}
