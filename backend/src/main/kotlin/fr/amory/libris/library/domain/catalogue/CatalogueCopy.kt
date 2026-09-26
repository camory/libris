package fr.amory.libris.library.domain.catalogue

import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.CopyId

data class CatalogueCopy(
    val copyId: CopyId,
    val bookshelfId: BookshelfId,
    val bookshelfName: String,
)
