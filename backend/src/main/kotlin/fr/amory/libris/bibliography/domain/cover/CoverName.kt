package fr.amory.libris.bibliography.domain.cover

@JvmInline
value class CoverName(val value: String) {
  init {
    require(PATTERN.matches(value)) { "a cover name is 64 lower-case hexadecimal digits" }
  }

  companion object {
    private val PATTERN = Regex("[0-9a-f]{64}")

    fun of(text: String): CoverName? = text.takeIf { PATTERN.matches(it) }?.let { CoverName(it) }
  }
}
