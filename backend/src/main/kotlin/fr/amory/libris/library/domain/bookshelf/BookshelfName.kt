package fr.amory.libris.library.domain.bookshelf

import java.text.Normalizer
import java.text.Normalizer.Form.NFD

@JvmInline
value class BookshelfName(val value: String) {
  init {
    require(value.isNotBlank()) { "a bookshelf name is not blank" }
  }

  fun isSameAs(other: BookshelfName): Boolean =
    folded(value) == folded(other.value)

  companion object {
    val ignoringCaseAndAccents: Comparator<BookshelfName> = compareBy { folded(it.value) }

    fun of(text: String): BookshelfName? =
      text.takeIf { it.isNotBlank() }?.let { BookshelfName(it) }
  }
}

private fun folded(name: String): String =
  Normalizer
    .normalize(name, NFD)
    .replace(COMBINING_MARKS, "")
    .lowercase()
    .replace("œ", "oe")
    .replace("æ", "ae")

private val COMBINING_MARKS = Regex("\\p{Mn}")
