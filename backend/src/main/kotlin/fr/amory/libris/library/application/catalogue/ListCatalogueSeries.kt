package fr.amory.libris.library.application.catalogue

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
      .mapNotNull { edition -> edition.series?.name }
      .groupingBy { it }
      .eachCount()
      .map { (name, volumeCount) -> CatalogueSeries(name, volumeCount, null, null) }
  }
}
