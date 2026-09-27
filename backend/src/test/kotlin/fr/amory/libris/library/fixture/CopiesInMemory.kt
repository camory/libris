package fr.amory.libris.library.fixture

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyRepository

class CopiesInMemory : CopyRepository {
    private val copies = mutableListOf<Copy>()

    val stored: List<Copy> get() = copies.toList()

    override fun insert(copy: Copy) {
        copies += copy
    }

    override fun findByEditionId(editionId: EditionId): List<Copy> = copies.filter { it.editionId == editionId }

    override fun findByBookshelfIds(bookshelfIds: List<BookshelfId>): List<Copy> =
        copies.filter { it.bookshelfId in bookshelfIds }
}
