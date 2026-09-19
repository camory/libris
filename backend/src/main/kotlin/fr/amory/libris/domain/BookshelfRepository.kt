package fr.amory.libris.domain

import java.util.UUID

interface BookshelfRepository {
    fun insert(bookshelf: Bookshelf)

    fun findByMember(readerId: UUID): List<Bookshelf>
}
