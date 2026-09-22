package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.CopyId

data class CopyView(
    val copyId: CopyId,
    val bookshelfId: BookshelfId,
    val bookshelfName: String,
)
