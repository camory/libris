package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.application.edition.EditEdition
import fr.amory.libris.bibliography.application.edition.EditEditionResult
import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
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
  private val copies: CopyRepository,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(readerId: ReaderId, editionId: EditionId, details: EditionDetails): SaveEditionResult =
    when (val result = editEdition(editionId, details)) {
      is Edited                       -> Saved(heldOf(readerId, result.edition))
      EditEditionResult.NoSuchEdition -> NoSuchEdition
    }

  private fun heldOf(readerId: ReaderId, edition: Edition): HeldEdition =
    HeldEdition(edition, seriesEntryOf(edition), readersCopiesOf(readerId, edition.id))

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

  private fun seriesEntryOf(edition: Edition): SeriesEntry? =
    edition.seriesVolume?.let { volume -> seriesRepository.findById(volume.seriesId)?.let(volume::entryIn) }
}
