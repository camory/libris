package fr.amory.libris.bibliography.domain.cover

class CoverFetches private constructor(private val all: List<CoverFetch>) {
  fun askedFor(chosenSource: CoverSource?): List<CoverFetch> =
    chosenSource
      ?.let { source -> all.filter { it.coverSource == source } }
      ?: all

  companion object {
    fun of(fetches: List<CoverFetch>): CoverFetches =
      CoverFetches(fetches.sortedBy { it.coverSource.order })
  }
}
