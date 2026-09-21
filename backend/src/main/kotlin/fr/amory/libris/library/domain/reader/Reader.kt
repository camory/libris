package fr.amory.libris.library.domain.reader

import fr.amory.libris.library.domain.bookshelf.BookshelfId

data class Reader(
    val id: ReaderId,
    val username: String,
    val email: String,
    val displayName: String,
    val defaultBookshelfId: BookshelfId,
)
