package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.reader.ReaderId

class BookshelvesInMemory : BookshelfRepository {
  private val bookshelves = mutableListOf<Bookshelf>()

  val stored: List<Bookshelf> get() = bookshelves.toList()

  override fun insert(bookshelf: Bookshelf) {
    bookshelves += bookshelf
  }

  override fun findById(id: BookshelfId): Bookshelf? = bookshelves.find { it.id == id }

  override fun findByMember(readerId: ReaderId): List<Bookshelf> = bookshelves.filter { it.hasMember(readerId) }
}
