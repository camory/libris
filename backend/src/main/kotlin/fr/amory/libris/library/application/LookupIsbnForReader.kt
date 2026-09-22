package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.application.IsbnLookupResult.Held
import fr.amory.libris.library.application.IsbnLookupResult.NotHeld
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class LookupIsbnForReader(
    private val editions: EditionRepository,
    private val copies: CopyRepository,
    private val bookshelves: BookshelfRepository,
    private val lookup: LookupEditionByIsbn,
) {
    fun lookUp(readerId: ReaderId, isbn: Isbn): IsbnLookupResult =
        editions.findByIsbn(isbn)
            ?.let { Held(it, copiesVisibleTo(readerId, it)) }
            ?: NotHeld(lookup.lookUp(isbn))

    private fun copiesVisibleTo(readerId: ReaderId, edition: Edition): List<CopyView> {
        val copiesOfEdition = copies.findByEditionId(edition.id)
        return copiesOfEdition
            .map { it.bookshelfId }
            .distinct()
            .mapNotNull { bookshelves.findById(it) }
            .filter { it.hasMember(readerId) }
            .sortedBy { it.name }
            .flatMap { bookshelf ->
                copiesOfEdition
                    .filter { it.bookshelfId == bookshelf.id }
                    .map { CopyView(it.id, bookshelf.id, bookshelf.name) }
            }
    }
}
