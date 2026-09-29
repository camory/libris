package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CatalogueEditions
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

private const val PAGE_SIZE = 50

@Service
class BrowseCatalogue(
    private val catalogueEditions: CatalogueEditions,
    private val bookshelves: BookshelfRepository,
    private val copies: CopyRepository,
    private val editions: EditionRepository,
) {
    operator fun invoke(readerId: ReaderId, after: EditionId?): CataloguePage {
        val page = catalogueEditions.findPage(readerId, after, PAGE_SIZE)
        val readersBookshelves = bookshelves.findByMember(readerId).associateBy { it.id }
        val editionsById = editions.findByIds(page.editionIds).associateBy { it.id }
        val copiesByEdition = copies.findByEditionIds(page.editionIds)
            .filter { it.bookshelfId in readersBookshelves }
            .groupBy { it.editionId }
        return CataloguePage(
            page.editionIds.map { editionId ->
                HeldEdition(
                    editionsById.getValue(editionId),
                    copiesByEdition.getValue(editionId).map { copy ->
                        CopyOnBookshelf(copy.id, copy.bookshelfId, readersBookshelves.getValue(copy.bookshelfId).name)
                    },
                )
            },
            page.next,
        )
    }
}
