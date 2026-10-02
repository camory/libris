package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.reader.ReaderId

interface BookshelfRepository {
  fun insert(bookshelf: Bookshelf)

  fun findById(id: BookshelfId): Bookshelf?

  fun findByMember(readerId: ReaderId): List<Bookshelf>
}
