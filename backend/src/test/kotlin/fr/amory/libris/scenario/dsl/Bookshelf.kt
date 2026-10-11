package fr.amory.libris.scenario.dsl

data class NewBook(
  val isbn13: String,
  val kind: String,
  val title: String,
  val subtitle: String? = null,
  val authors: List<NewAuthor> = emptyList(),
  val series: NewSeries? = null,
  val collection: String? = null,
  val publisher: String? = null,
  val publicationYear: Int? = null,
  val language: String? = null,
  val pageCount: Int? = null,
  val summary: String? = null,
  val coverSource: String? = null)

data class NewAuthor(val name: String, val role: String)

data class NewSeries(val name: String, val volumeNumber: Int?)

data class Copy(val id: String, val bookshelf: Bookshelf)

fun Reader.adds(book: NewBook, to: Bookshelf): Answer<Copy> =
  libris.post(this, "/api/v1/bookshelves/${to.id}/books", book) { read<Copy>(it) }

fun Reader.hasAdded(book: NewBook, to: Bookshelf): Copy =
  adds(book, to).value
