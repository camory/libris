package fr.amory.libris.scenario.dsl

data class NewBook(
  val isbn13: String,
  val kind: String,
  val title: String,
  val subtitle: String? = null,
  val authors: List<Author> = emptyList(),
  val series: Series? = null,
  val collection: String? = null,
  val publisher: String? = null,
  val publicationYear: Int? = null,
  val language: String? = null,
  val pageCount: Int? = null,
  val summary: String? = null,
  val coverSource: String? = null)

data class Copy(val id: String, val bookshelf: Bookshelf)

fun Reader.adds(book: NewBook, to: Bookshelf): Answer<Copy> =
  libris.post(this, "/api/v1/bookshelves/${to.id}/books", book) { read<Copy>(it) }

fun Reader.hasAdded(book: NewBook, to: Bookshelf): Copy =
  adds(book, to).value
