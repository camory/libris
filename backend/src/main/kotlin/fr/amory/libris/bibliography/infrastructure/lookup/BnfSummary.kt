package fr.amory.libris.bibliography.infrastructure.lookup

private val LINE_BREAK = Regex("</p><p(?:\\s[^>]*)?>|<br\\s*/?>")
private val TAG = Regex("<[^>]*>")
private val ENTITY = Regex("&(?:#(\\d+)|#[xX](\\p{XDigit}+)|(amp|lt|gt|quot|apos|nbsp));")
private val NAMED = mapOf("amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to "\u00A0")
private val LINE_FEEDS = Regex("\n{2,}")
private val SPACES = Regex("[ \u00A0]{2,}")
private val GLUED_SENTENCE = Regex("([.!?\u2026])(\\p{Lu}|\u2022)")
private const val HEXADECIMAL = 16

internal fun summaryOf(description: String?): String? =
  description
    ?.replace(LINE_BREAK, "\n")
    ?.replace(TAG, "")
    ?.replace(ENTITY) { characterOf(it) }
    ?.replace(LINE_FEEDS, "\n")
    ?.replace(SPACES, " ")
    ?.replace(GLUED_SENTENCE, "$1 $2")
    ?.trim()
    ?.ifEmpty { null }

private fun characterOf(entity: MatchResult): String {
  val (decimal, hexadecimal, name) = entity.destructured
  return when {
    decimal.isNotEmpty()     -> characterAt(decimal.toIntOrNull()) ?: entity.value
    hexadecimal.isNotEmpty() -> characterAt(hexadecimal.toIntOrNull(HEXADECIMAL)) ?: entity.value
    else                     -> NAMED.getValue(name)
  }
}

private fun characterAt(codePoint: Int?): String? =
  codePoint
    ?.takeIf { Character.isValidCodePoint(it) }
    ?.let { Character.toString(it) }
