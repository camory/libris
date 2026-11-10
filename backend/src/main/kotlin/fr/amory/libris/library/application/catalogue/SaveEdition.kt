package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.application.edition.EditEdition
import fr.amory.libris.bibliography.application.edition.EditEditionResult
import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.ReaderBookshelf
import fr.amory.libris.library.application.catalogue.SaveEditionResult.NoSuchEdition
import fr.amory.libris.library.application.catalogue.SaveEditionResult.Saved
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class SaveEdition(
  private val editEdition: EditEdition,
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository) {
  operator fun invoke(readerId: ReaderId, editionId: EditionId, details: EditionDetails): SaveEditionResult =
    when (val result = editEdition(editionId, details)) {
      is Edited                       -> Saved(HeldEdition(result.edition, null, readersCopiesOf(readerId, editionId)))
      EditEditionResult.NoSuchEdition -> NoSuchEdition
    }

  private fun readersCopiesOf(readerId: ReaderId, editionId: EditionId): List<CopyOnBookshelf> {
    val readersBookshelves = bookshelves
      .findByMember(readerId)
      .mapNotNull { ReaderBookshelf.of(it, readerId) }
      .associateBy { it.id }
    return copies
      .findByEditionId(editionId)
      .filter { it.bookshelfId in readersBookshelves }
      .map { CopyOnBookshelf(it.id, readersBookshelves.getValue(it.bookshelfId)) }
  }
}
