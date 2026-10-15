package fr.amory.libris.scenario.dsl

data class Book(
  val isbn13: String?,
  val title: String,
  val series: SeriesEntry?,
  val copies: List<Copy>,
  val coverUrl: String?)

data class BookPage(val books: List<Book>, val next: String?)

fun Reader.catalogue(after: String? = null): BookPage =
  libris.get(this, "/api/v1/books" + after?.let { "?after=$it" }.orEmpty()) { read<BookPage>(it) }.value

fun Reader.seesCoverOf(isbn: String): String? =
  catalogue().books.single { it.isbn13 == isbn }.coverUrl

fun Reader.awaitsCoverOf(isbn: String): String {
  await { seesCoverOf(isbn) != null }
  return checkNotNull(seesCoverOf(isbn))
}
