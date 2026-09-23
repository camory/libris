package fr.amory.libris.library.application.lookup

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class LookupIsbnForReader(
    private val editionLookup: LookupEditionByIsbn,
    private val copies: CopyRepository,
    private val bookshelves: BookshelfRepository,
) {
    fun lookUp(readerId: ReaderId, isbn: Isbn): IsbnLookup {
        val answer = editionLookup.lookUp(isbn)
        return IsbnLookup(answer, copiesVisibleTo(readerId, answer))
    }

    private fun copiesVisibleTo(readerId: ReaderId, answer: EditionLookupResult): List<CopyOnBookshelf> =
        if (answer is Held) copiesVisibleTo(readerId, answer.edition) else emptyList()

    private fun copiesVisibleTo(readerId: ReaderId, edition: Edition): List<CopyOnBookshelf> {
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
                    .map { CopyOnBookshelf(it.id, bookshelf.id, bookshelf.name) }
            }
    }
}
