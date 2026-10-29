package fr.amory.libris.bibliography.infrastructure.lookup

private val TAG = Regex("<[^>]*>")
private val ENTITY = Regex("&(?:#(\\d+)|#[xX](\\p{XDigit}+)|(amp|lt|gt|quot|apos|nbsp));")
private val NAMED = mapOf("amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'", "nbsp" to " ")
private const val HEXADECIMAL = 16

internal fun summaryOf(description: String?): String? =
  description
    ?.replace(TAG, "")
    ?.replace(ENTITY) { characterOf(it) }

private fun characterOf(entity: MatchResult): String {
  val (decimal, hexadecimal, name) = entity.destructured
  return when {
    decimal.isNotEmpty()     -> Character.toString(decimal.toInt())
    hexadecimal.isNotEmpty() -> Character.toString(hexadecimal.toInt(HEXADECIMAL))
    else                     -> NAMED.getValue(name)
  }
}
