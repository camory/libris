package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface CoverLookup {
  fun lookUpCover(isbn: Isbn): CoverCandidate?
}
