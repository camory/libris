package fr.amory.libris.domain

interface BookshelfRepository {
    fun insert(bookshelf: Bookshelf)
}
