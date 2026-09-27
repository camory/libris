package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.Edition.Companion.BY_SERIES_AND_VOLUME
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class BrowseCatalogue(
    private val bookshelves: BookshelfRepository,
    private val copies: CopyRepository,
    private val editions: EditionRepository,
) {
    operator fun invoke(readerId: ReaderId): List<HeldEdition> {
        val readersBookshelves = bookshelves.findByMember(readerId).associateBy { it.id }
        val held = copies.findByBookshelfIds(readersBookshelves.keys.toList())
        val editionsById = editions.findByIds(held.map { it.editionId }.distinct()).associateBy { it.id }
        return held.groupBy { it.editionId }.map { (editionId, copiesOfEdition) ->
            HeldEdition(
                editionsById.getValue(editionId),
                copiesOfEdition.map { copy ->
                    CopyOnBookshelf(copy.id, copy.bookshelfId, readersBookshelves.getValue(copy.bookshelfId).name)
                },
            )
        }.sortedWith(compareBy(BY_SERIES_AND_VOLUME) { it.edition })
    }
}
