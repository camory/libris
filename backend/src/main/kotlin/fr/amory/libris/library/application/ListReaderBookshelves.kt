package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class ListReaderBookshelves(private val bookshelves: BookshelfRepository) {
  operator fun invoke(reader: Reader): List<ReaderBookshelf> =
    bookshelves
      .findByMember(reader.id)
      .mapNotNull { readerBookshelf(it, reader.id) }

  private fun readerBookshelf(bookshelf: Bookshelf, readerId: ReaderId): ReaderBookshelf? =
    bookshelf
      .roleOf(readerId)
      ?.let { ReaderBookshelf(bookshelf.id, bookshelf.name, it) }
}
