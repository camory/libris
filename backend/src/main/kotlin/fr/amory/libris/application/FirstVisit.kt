package fr.amory.libris.application

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FirstVisit(
    private val readers: ReaderRepository,
    private val bookshelves: BookshelfRepository,
) {
    @Transactional
    fun welcome(username: String, email: String, displayName: String): Reader {
        val bookshelf = Bookshelf(name = "Bibliothèque de $displayName")
        bookshelves.insert(bookshelf)
        val reader = Reader(username, email, displayName, bookshelf)
        readers.insert(reader)
        return reader
    }
}
