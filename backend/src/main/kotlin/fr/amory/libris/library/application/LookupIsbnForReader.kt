package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.library.application.IsbnLookupResult.Held
import fr.amory.libris.library.application.IsbnLookupResult.NotHeld
import fr.amory.libris.library.domain.lookup.ReaderCopies
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class LookupIsbnForReader(
    private val editions: EditionRepository,
    private val readerCopies: ReaderCopies,
    private val lookup: LookupEditionByIsbn,
) {
    fun lookUp(readerId: ReaderId, isbn: Isbn): IsbnLookupResult =
        editions.findByIsbn(isbn)
            ?.let { Held(it, readerCopies.ofEdition(it.id, readerId)) }
            ?: NotHeld(lookup.lookUp(isbn))
}
