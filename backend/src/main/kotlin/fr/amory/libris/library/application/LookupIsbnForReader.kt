package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.library.application.IsbnLookupResult.Found
import fr.amory.libris.library.application.IsbnLookupResult.SourcesUnavailable
import fr.amory.libris.library.application.IsbnLookupResult.UnknownIsbn
import org.springframework.stereotype.Service

@Service
class LookupIsbnForReader(private val lookup: LookupEditionByIsbn) {
    fun lookUp(isbn: Isbn): IsbnLookupResult = when (val answer = lookup.lookUp(isbn)) {
        is EditionLookupResult.Found -> Found(answer.preview, emptyList())
        EditionLookupResult.UnknownIsbn -> UnknownIsbn
        EditionLookupResult.SourcesUnavailable -> SourcesUnavailable
    }
}
