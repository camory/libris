package fr.amory.libris.library.application

import fr.amory.libris.library.application.ChooseDefaultResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.stereotype.Service

@Service
class ChooseDefaultBookshelf {
  @Suppress("UnusedParameter")
  operator fun invoke(reader: Reader, bookshelfId: BookshelfId): ChooseDefaultResult =
    NoSuchBookshelf
}
