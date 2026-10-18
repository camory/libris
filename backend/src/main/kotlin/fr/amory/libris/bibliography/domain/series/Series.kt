package fr.amory.libris.bibliography.domain.series

data class Series(
  val id: SeriesId,
  val name: String) {
  init {
    require(name.isNotBlank()) { "a series needs a name" }
  }
}
