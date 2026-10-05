package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.Isbn

interface CoverFetch {
  val coverSource: CoverSource

  fun coverFor(isbn: Isbn): Cover?
}
