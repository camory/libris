package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class ListCatalogueSeries(
  private val catalogueEditions: CatalogueEditions,
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository,
  private val editions: EditionRepository,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(readerId: ReaderId): List<CatalogueSeries> {
    val readersBookshelves = bookshelves.findByMember(readerId)
    val readersCopies = copies.findByBookshelfIds(readersBookshelves.map { it.id })
    val firstOfEachSeries = catalogueEditions.findFirstOfEachSeries(readerId)
    val editionsBySeriesId = editions
      .findByIds(readersCopies.map { it.editionId })
      .mapNotNull { edition -> edition.seriesVolume?.let { it.seriesId to edition } }
      .groupBy({ it.first }, { it.second })
    val seriesById = seriesRepository
      .findByIds(editionsBySeriesId.keys.toList())
      .associateBy { it.id }
    return editionsBySeriesId.map { (seriesId, seriesEditions) ->
      seriesOf(seriesById.getValue(seriesId), seriesEditions, firstOfEachSeries)
    }
  }

  private fun seriesOf(series: Series, seriesEditions: List<Edition>, firstOfEachSeries: Set<EditionId>) =
    CatalogueSeries(
      series.id,
      series.name,
      seriesEditions.size,
      coverName = seriesEditions.firstOrNull { it.id in firstOfEachSeries }?.coverName,
      kind = seriesEditions.map { it.kind }.distinct().singleOrNull(),
    )
}
