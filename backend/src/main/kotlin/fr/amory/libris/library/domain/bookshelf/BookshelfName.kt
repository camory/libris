package fr.amory.libris.library.domain.bookshelf

import java.text.Normalizer
import java.text.Normalizer.Form.NFD

@JvmInline
value class BookshelfName(val value: String) : Comparable<BookshelfName> {
  init {
    require(value.isNotBlank()) { "a bookshelf name is not blank" }
  }

  fun isSameAs(other: BookshelfName): Boolean =
    compareTo(other) == 0

  override fun compareTo(other: BookshelfName): Int =
    ignoringCaseAndAccents(value).compareTo(ignoringCaseAndAccents(other.value))

  companion object {
    fun of(text: String): BookshelfName? =
      text.takeIf { it.isNotBlank() }?.let { BookshelfName(it) }
  }
}

private fun ignoringCaseAndAccents(name: String): String =
  Normalizer
    .normalize(name, NFD)
    .replace(COMBINING_MARKS, "")
    .lowercase()
    .replace("œ", "oe")
    .replace("æ", "ae")

private val COMBINING_MARKS = Regex("\\p{Mn}")
