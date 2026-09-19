package fr.amory.libris.fixture

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository
import java.util.UUID

class BookshelvesInMemory : BookshelfRepository {
    private val bookshelves = mutableListOf<Bookshelf>()

    val stored: List<Bookshelf> get() = bookshelves.toList()

    override fun insert(bookshelf: Bookshelf) {
        bookshelves += bookshelf
    }

    override fun findByMember(readerId: UUID): List<Bookshelf> =
        bookshelves.filter { bookshelf -> bookshelf.members.any { it.readerId == readerId } }
}
