package fr.amory.libris.library.application

import fr.amory.libris.library.application.OpenBookshelfResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class OpenBookshelf {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId): OpenBookshelfResult =
    NoSuchBookshelf
}
