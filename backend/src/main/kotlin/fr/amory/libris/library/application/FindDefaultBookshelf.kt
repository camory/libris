package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.stereotype.Service

@Service
class FindDefaultBookshelf(private val bookshelves: BookshelfRepository) {
    operator fun invoke(reader: Reader): Bookshelf =
        checkNotNull(bookshelves.findById(reader.defaultBookshelfId)) { "a reader has a default bookshelf" }
}
