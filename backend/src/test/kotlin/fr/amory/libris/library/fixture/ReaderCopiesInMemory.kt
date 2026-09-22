package fr.amory.libris.library.fixture

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.lookup.ReaderCopies
import fr.amory.libris.library.domain.reader.ReaderId

class ReaderCopiesInMemory : ReaderCopies {
    private val visible = mutableMapOf<Pair<EditionId, ReaderId>, List<CopyOnBookshelf>>()

    fun visibleTo(readerId: ReaderId, editionId: EditionId, copies: List<CopyOnBookshelf>) {
        visible[editionId to readerId] = copies
    }

    override fun ofEdition(editionId: EditionId, readerId: ReaderId): List<CopyOnBookshelf> =
        visible[editionId to readerId].orEmpty()
}
