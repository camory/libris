package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.library.application.catalogue.FindCatalogueEditionResult.Found
import fr.amory.libris.library.application.catalogue.FindCatalogueEditionResult.NoSuchEdition
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class FindCatalogueEdition(
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository,
  private val editions: EditionRepository,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(readerId: ReaderId, editionId: EditionId): FindCatalogueEditionResult {
    val readersCopies = readersCopiesOf(readerId, editionId)
    if (readersCopies.isEmpty()) return NoSuchEdition
    val edition = editions
      .findByIds(listOf(editionId))
      .single()
    return Found(HeldEdition(edition, seriesEntryOf(edition), readersCopies))
  }

  private fun readersCopiesOf(readerId: ReaderId, editionId: EditionId): List<CopyOnBookshelf> {
    val readersBookshelves = bookshelves
      .findByMember(readerId)
      .associateBy { it.id }
    return copies
      .findByEditionId(editionId)
      .filter { it.bookshelfId in readersBookshelves }
      .map { CopyOnBookshelf(it.id, it.bookshelfId, readersBookshelves.getValue(it.bookshelfId).name.value) }
  }

  private fun seriesEntryOf(edition: Edition): SeriesEntry? =
    edition.seriesVolume?.let { volume -> seriesRepository.findById(volume.seriesId)?.let(volume::entryIn) }
}
