package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId

data class Copy(
    val id: CopyId,
    val editionId: EditionId,
    val bookshelfId: BookshelfId,
)
