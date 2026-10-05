package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.Isbn

interface CoverLookup {
  fun candidateFor(isbn: Isbn): CoverCandidate?
}
