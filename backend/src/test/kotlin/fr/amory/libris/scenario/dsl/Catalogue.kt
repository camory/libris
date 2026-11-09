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

fun Reader.catalogue(after: String? = null): BookPage =
  libris.get(this, "/api/v1/books" + after?.let { "?after=$it" }.orEmpty()) { read<BookPage>(it) }.value

fun Reader.bookOf(isbn: String): Book =
  catalogue().books.single { it.isbn13 == isbn }

fun Reader.opens(book: Book): Answer<Book> =
  libris.get(this, "/api/v1/books/${book.id}") { read<Book>(it) }

fun Reader.saves(book: Book, edited: EditionDetails): Answer<Book> =
  libris.put(this, "/api/v1/books/${book.id}", edited) { read<Book>(it) }

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
