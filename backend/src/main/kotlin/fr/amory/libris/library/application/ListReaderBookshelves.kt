package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.stereotype.Service

@Service
class ListReaderBookshelves(private val bookshelves: BookshelfRepository) {
  operator fun invoke(reader: Reader): List<ReaderBookshelf> =
    bookshelves
      .findByMember(reader.id)
      .mapNotNull { ReaderBookshelf.of(it, reader.id) }
}
