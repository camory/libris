package fr.amory.libris.scenario.dsl

data class Card(
  val id: String?,
  val isbn13: String,
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
  val summary: String?,
  val covers: List<Cover>,
  val copies: List<Copy>)

data class Cover(val source: String, val url: String)

fun Reader.asks(isbn: String): Answer<Card> =
  libris.get(this, "/api/v1/isbn/$isbn") { read<Card>(it) }
