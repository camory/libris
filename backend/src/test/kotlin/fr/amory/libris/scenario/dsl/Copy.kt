package fr.amory.libris.scenario.dsl

import java.util.UUID

private data class CopyWithRole(val id: String, val bookshelf: ReaderBookshelf)

private data class BookCopies(val copies: List<CopyWithRole>)

fun Reader.removes(copy: Copy): Answer<Unit> =
  libris.delete(this, "/api/v1/copies/${copy.id}")

fun Reader.rolesOn(book: Book): Map<Copy, Role> =
  libris.get(this, "/api/v1/books/${book.id}") { read<BookCopies>(it) }.value.copies.associate {
    Copy(it.id, Bookshelf(it.bookshelf.id, it.bookshelf.name)) to it.bookshelf.role
  }

fun Libris.unknownCopy() =
  Copy(UUID.randomUUID().toString(), Bookshelf(UUID.randomUUID().toString(), "Inconnue"))
