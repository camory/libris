package fr.amory.libris.library.domain.lookup

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.domain.reader.ReaderId

interface ReaderCopies {
    fun ofEdition(editionId: EditionId, readerId: ReaderId): List<CopyOnBookshelf>
}
