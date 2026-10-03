package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.Isbn

interface CoverFetch {
  fun fetch(isbn: Isbn): Cover?
}
