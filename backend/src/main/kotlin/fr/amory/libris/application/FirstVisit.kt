package fr.amory.libris.application

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.BookshelfRepository
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole.OWNER
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import fr.amory.libris.domain.newId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FirstVisit(
    private val readers: ReaderRepository,
    private val bookshelves: BookshelfRepository,
) {
    @Transactional
    fun welcome(username: String, email: String, displayName: String): Reader {
        val bookshelfId = newId()
        val reader = Reader(
            username = username,
            email = email,
            displayName = displayName,
            defaultBookshelfId = bookshelfId,
        )
        val bookshelf = Bookshelf(
            id = bookshelfId,
            name = "Bibliothèque de $displayName",
            members = listOf(Member(reader.id, OWNER)),
        )
        readers.insert(reader)
        bookshelves.insert(bookshelf)
        return reader
    }
}
