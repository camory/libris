package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.library.application.ReaderBookshelf
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId

internal fun readersCopiesOf(
  bookshelves: BookshelfRepository,
  copies: CopyRepository,
  readerId: ReaderId,
  editionId: EditionId): List<CopyOnBookshelf> {
  val readersBookshelves = readersBookshelvesOf(bookshelves, readerId)
  return copies
    .findByEditionId(editionId)
    .filter { it.bookshelfId in readersBookshelves }
    .map { CopyOnBookshelf(it.id, readersBookshelves.getValue(it.bookshelfId)) }
}

internal fun readersBookshelvesOf(
  bookshelves: BookshelfRepository,
  readerId: ReaderId): Map<BookshelfId, ReaderBookshelf> =
  bookshelves
    .findByMember(readerId)
    .mapNotNull { ReaderBookshelf.of(it, readerId) }
    .associateBy { it.id }

internal fun seriesEntryOf(seriesRepository: SeriesRepository, edition: Edition): SeriesEntry? =
  edition.seriesVolume?.let { volume -> seriesRepository.findById(volume.seriesId)?.let(volume::entryIn) }
