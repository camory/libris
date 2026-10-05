package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.Isbn

class CoverFetches private constructor(private val all: List<CoverFetch>) {
  fun coverFor(isbn: Isbn, chosenSource: CoverSource?): Cover? =
    askedFor(chosenSource).firstNotNullOfOrNull { it.coverFor(isbn) }

  private fun askedFor(chosenSource: CoverSource?): List<CoverFetch> =
    chosenSource
      ?.let { source -> all.filter { it.coverSource == source } }
      ?: all

  companion object {
    fun of(fetches: List<CoverFetch>): CoverFetches =
      CoverFetches(fetches.sortedBy { it.coverSource.order })
  }
}
