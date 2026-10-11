package fr.amory.libris.scenario.dsl

data class Book(
  val isbn13: String?,
  val title: String,
  val series: Series?,
  val copies: List<Copy>,
  val coverUrl: String?)

data class BookPage(val books: List<Book>, val next: String?)

fun Reader.catalogue(after: String? = null): BookPage =
  libris.get(this, "/api/v1/books" + after?.let { "?after=$it" }.orEmpty()) { read<BookPage>(it) }.value
