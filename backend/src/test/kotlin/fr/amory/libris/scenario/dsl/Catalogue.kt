package fr.amory.libris.scenario.dsl

data class Book(val isbn13: String?, val title: String, val copies: List<Copy>)

data class BookPage(val books: List<Book>, val next: String?)

fun Reader.catalogue(): BookPage =
  libris.get(this, "/api/v1/books") { read<BookPage>(it) }.value
