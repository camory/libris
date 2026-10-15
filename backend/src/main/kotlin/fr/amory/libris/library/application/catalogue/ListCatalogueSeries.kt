package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class ListCatalogueSeries(
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository,
  private val editions: EditionRepository) {
  operator fun invoke(readerId: ReaderId): List<CatalogueSeries> {
    val readersBookshelves = bookshelves.findByMember(readerId)
    val readersCopies = copies.findByBookshelfIds(readersBookshelves.map { it.id })
    return editions
      .findByIds(readersCopies.map { it.editionId })
      .mapNotNull { edition -> edition.series?.let { it.name to edition } }
      .groupBy({ it.first }, { it.second })
      .map { (name, seriesEditions) -> seriesOf(name, seriesEditions) }
  }

  private fun seriesOf(name: String, seriesEditions: List<Edition>) =
    CatalogueSeries(
      name,
      seriesEditions.size,
      coverName = null,
      kind = seriesEditions.map { it.kind }.distinct().singleOrNull(),
    )
}
