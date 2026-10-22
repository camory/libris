package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

private const val PAGE_SIZE = 50

@Service
class BrowseCatalogue(
  private val catalogueEditions: CatalogueEditions,
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository,
  private val editions: EditionRepository,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(readerId: ReaderId, after: EditionId?): CataloguePage {
    val page = catalogueEditions.findPage(readerId, after, PAGE_SIZE)
    val readersBookshelves = bookshelves.findByMember(readerId).associateBy { it.id }
    val editionsById = editions.findByIds(page.editionIds).associateBy { it.id }
    val seriesById = seriesRepository
      .findByIds(editionsById.values.mapNotNull { it.seriesVolume?.seriesId }.distinct())
      .associateBy { it.id }
    val copiesByEdition = copies
      .findByEditionIds(page.editionIds)
      .filter { it.bookshelfId in readersBookshelves }
      .groupBy { it.editionId }
    return CataloguePage(
      page.editionIds.map { editionId ->
        val edition = editionsById.getValue(editionId)
        HeldEdition(
          edition,
          edition.seriesVolume?.let { volume -> seriesById[volume.seriesId]?.let(volume::entryIn) },
          copiesByEdition
            .getValue(editionId)
            .map { CopyOnBookshelf.of(it, readersBookshelves.getValue(it.bookshelfId)) },
        )
      },
      page.next,
    )
  }
}
