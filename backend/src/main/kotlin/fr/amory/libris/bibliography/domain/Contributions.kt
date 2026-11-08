package fr.amory.libris.bibliography.domain

import java.text.Normalizer
import java.text.Normalizer.Form.NFD

@JvmInline
value class Contributions private constructor(private val all: Set<Contribution>) : Iterable<Contribution> {
  override fun iterator(): Iterator<Contribution> =
    all.iterator()

  fun isEmpty(): Boolean =
    all.isEmpty()

  companion object {
    fun of(contributions: List<Contribution>): Contributions =
      Contributions(
        contributions
          .distinctBy { folded(it.name) to it.role }
          .toSet(),
      )
  }
}

private fun folded(name: String): String =
  Normalizer
    .normalize(name, NFD)
    .replace(COMBINING_MARKS, "")
    .lowercase()

private val COMBINING_MARKS = Regex("\\p{Mn}")
