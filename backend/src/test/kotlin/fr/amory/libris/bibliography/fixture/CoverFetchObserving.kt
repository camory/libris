package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource

class CoverFetchObserving<S>(override val coverSource: CoverSource, private val observe: () -> S) : CoverFetch {
  private val observed = mutableListOf<S>()

  val asks: List<S> get() = observed.toList()

  override fun coverFor(isbn: Isbn): Cover? {
    observed += observe()
    return null
  }
}
