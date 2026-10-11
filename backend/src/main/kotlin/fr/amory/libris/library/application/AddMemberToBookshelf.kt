package fr.amory.libris.library.application

import fr.amory.libris.library.application.AddMemberResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class AddMemberToBookshelf {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, bookshelfId: BookshelfId, membership: Membership): AddMemberResult =
    NoSuchBookshelf
}
