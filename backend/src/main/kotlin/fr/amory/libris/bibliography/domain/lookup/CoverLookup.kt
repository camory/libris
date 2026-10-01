package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface CoverLookup {
  fun lookUp(isbn: Isbn): CoverCandidate?
}
