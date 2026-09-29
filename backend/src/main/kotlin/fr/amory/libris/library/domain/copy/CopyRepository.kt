package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId

interface CopyRepository {
    fun insert(copy: Copy)

    fun findByEditionId(editionId: EditionId): List<Copy>

    fun findByEditionIds(editionIds: List<EditionId>): List<Copy>

    fun findByBookshelfIds(bookshelfIds: List<BookshelfId>): List<Copy>
}
