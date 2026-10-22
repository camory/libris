package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.Found
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.NoSuchSeries
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class FindCatalogueSeries(
  private val catalogueEditions: CatalogueEditions,
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository,
  private val editions: EditionRepository,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(readerId: ReaderId, seriesId: SeriesId): FindCatalogueSeriesResult {
    val readersBookshelves = bookshelves
      .findByMember(readerId)
      .associateBy { it.id }
    val copiesByEditionId = copies
      .findByBookshelfIds(readersBookshelves.keys.toList())
      .groupBy { it.editionId }
    val seriesEditions = editions
      .findByIds(copiesByEditionId.keys.toList())
      .filter { it.seriesVolume?.seriesId == seriesId }
    if (seriesEditions.isEmpty()) return NoSuchSeries
    val series = checkNotNull(seriesRepository.findById(seriesId))
    return Found(
      catalogueSeriesOf(series, seriesEditions, catalogueEditions.findFirstOfEachSeries(readerId)),
      seriesEditions.map { edition ->
        HeldEdition(
          edition,
          edition.seriesVolume?.entryIn(series),
          copiesByEditionId
            .getValue(edition.id)
            .map { CopyOnBookshelf.of(it, readersBookshelves.getValue(it.bookshelfId)) },
        )
      },
    )
  }
}
