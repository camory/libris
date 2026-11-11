package fr.amory.libris.scenario.dsl

import java.util.UUID

data class Book(
  val id: String,
  val isbn13: String?,
  val title: String,
  val subtitle: String?,
  val authors: List<Author>,
  val series: SeriesEntry?,
  val copies: List<Copy>,
  val coverUrl: String?)

data class BookPage(val books: List<Book>, val next: String?)

data class SearchPage(val books: List<Book>, val next: String?, val near: Boolean)

fun Reader.catalogue(after: String? = null): BookPage =
  libris.get(this, "/api/v1/books" + after?.let { "?after=$it" }.orEmpty()) { read<BookPage>(it) }.value

fun Reader.searches(words: String, after: String? = null): SearchPage =
  libris.get(this, "/api/v1/books?q={words}" + after?.let { "&after=$it" }.orEmpty(), words) {
    read<SearchPage>(it)
  }.value

fun Reader.bookOf(isbn: String): Book =
  catalogue().books.single { it.isbn13 == isbn }

fun Reader.catalogueRowsOf(titles: List<String>): List<Book> =
  catalogue().books.let { rows -> titles.map { title -> rows.single { it.title == title } } }

fun Reader.opens(book: Book): Answer<Book> =
  libris.get(this, "/api/v1/books/${book.id}") { read<Book>(it) }

fun Reader.saves(book: Book, edited: EditionDetails): Answer<Book> =
  libris.put(this, "/api/v1/books/${book.id}", edited) { read<Book>(it) }

fun Reader.hasSaved(book: Book, edited: EditionDetails): Book =
  saves(book, edited).value

fun Libris.unknownBook() =
  Book(
    id = UUID.randomUUID().toString(),
    isbn13 = null,
    title = "Inconnu",
    subtitle = null,
    authors = emptyList(),
    series = null,
    copies = emptyList(),
    coverUrl = null,
  )

fun Reader.seesCoverOf(isbn: String): String? =
  bookOf(isbn).coverUrl

fun Reader.awaitsCoverOf(isbn: String): String {
  await { seesCoverOf(isbn) != null }
  return checkNotNull(seesCoverOf(isbn))
}
