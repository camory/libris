package fr.amory.libris.library.application

import fr.amory.libris.library.application.FindBookshelfResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class FindBookshelf {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId): FindBookshelfResult =
    NoSuchBookshelf
}
