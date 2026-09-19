package fr.amory.libris.domain

import java.util.UUID

interface BookshelfRepository {
    fun insert(bookshelf: Bookshelf)

    fun findById(id: UUID): Bookshelf?
}
