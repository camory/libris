package fr.amory.libris.library.domain.bookshelf

interface BookshelfRepository {
    fun insert(bookshelf: Bookshelf)

    fun findById(id: BookshelfId): Bookshelf?
}
