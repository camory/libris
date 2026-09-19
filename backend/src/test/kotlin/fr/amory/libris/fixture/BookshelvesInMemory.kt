package fr.amory.libris.fixture

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository

class BookshelvesInMemory : BookshelfRepository {
    private val bookshelves = mutableListOf<Bookshelf>()

    val stored: List<Bookshelf> get() = bookshelves.toList()

    override fun insert(bookshelf: Bookshelf) {
        bookshelves += bookshelf
    }
}
