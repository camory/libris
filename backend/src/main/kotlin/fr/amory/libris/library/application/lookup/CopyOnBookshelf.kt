package fr.amory.libris.library.application.lookup

import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.CopyId

data class CopyOnBookshelf(
    val copyId: CopyId,
    val bookshelfId: BookshelfId,
    val bookshelfName: String,
)
