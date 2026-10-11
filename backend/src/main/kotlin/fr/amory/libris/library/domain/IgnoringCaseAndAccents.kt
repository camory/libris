package fr.amory.libris.library.domain

import java.text.Normalizer
import java.text.Normalizer.Form.NFD

object IgnoringCaseAndAccents : Comparator<String> {
  override fun compare(first: String, second: String): Int =
    folded(first).compareTo(folded(second))
}

private fun folded(text: String): String =
  Normalizer
    .normalize(text, NFD)
    .replace(COMBINING_MARKS, "")
    .lowercase()
    .replace("œ", "oe")
    .replace("æ", "ae")

private val COMBINING_MARKS = Regex("\\p{Mn}")
