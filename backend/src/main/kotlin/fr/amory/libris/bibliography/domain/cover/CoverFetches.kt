package fr.amory.libris.bibliography.domain.cover

class CoverFetches private constructor(private val all: List<CoverFetch>) {
  fun askedFor(chosenSource: CoverSource?): List<CoverFetch> =
    all.filter { it.coverSource == chosenSource }

  companion object {
    fun of(fetches: List<CoverFetch>): CoverFetches =
      CoverFetches(fetches)
  }
}
