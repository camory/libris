package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface ExternalCoverLookup {
    fun lookUp(isbn: Isbn): CoverCandidate?
}
