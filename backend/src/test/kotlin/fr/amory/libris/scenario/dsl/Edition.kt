package fr.amory.libris.scenario.dsl

data class Author(val name: String, val role: String)

data class SeriesEntry(val name: String, val volumeNumber: Int?)

data class EditionDetails(
  val kind: String,
  val title: String,
  val subtitle: String?,
  val authors: List<Author>,
  val series: SeriesEntry?,
  val collection: String?,
  val publisher: String?,
  val publicationYear: Int?,
  val language: String?,
  val pageCount: Int?,
  val summary: String?)

val NewBook.details: EditionDetails
  get() = EditionDetails(
    kind, title, subtitle, authors, series, collection, publisher, publicationYear, language, pageCount, summary,
  )
