package fr.amory.libris.scenario.dsl

enum class Role { OWNER, VIEWER }

data class Bookshelf(val id: String, val name: String)

data class ReaderBookshelf(val id: String, val name: String, val role: Role)

data class BookshelfWithMembers(val id: String, val name: String, val members: List<Member>)

data class KnownReader(val id: String, val displayName: String)

data class Member(val id: String, val displayName: String, val role: Role)

fun Reader.creates(name: String): Answer<Bookshelf> =
  libris.post(this, "/api/v1/bookshelves", mapOf("name" to name)) { read<Bookshelf>(it) }

fun Reader.hasCreated(name: String): Bookshelf =
  creates(name).value

fun Reader.bookshelves(): List<ReaderBookshelf> =
  libris.get(this, "/api/v1/bookshelves") { read<List<ReaderBookshelf>>(it) }.value

fun Reader.opens(bookshelf: Bookshelf): Answer<BookshelfWithMembers> =
  libris.get(this, "/api/v1/bookshelves/${bookshelf.id}") { read<BookshelfWithMembers>(it) }

fun Reader.readers(): List<KnownReader> =
  libris.get(this, "/api/v1/readers") { read<List<KnownReader>>(it) }.value

fun Reader.adds(reader: KnownReader, role: Role, to: Bookshelf): Answer<Member> =
  libris.post(this, "/api/v1/bookshelves/${to.id}/members", mapOf("readerId" to reader.id, "role" to role)) {
    read<Member>(it)
  }

fun Reader.adds(reader: Reader, role: Role, to: Bookshelf): Answer<Member> =
  adds(KnownReader(reader.id, reader.displayName), role, to)

fun Reader.hasAdded(reader: Reader, role: Role, to: Bookshelf): Member =
  adds(reader, role, to).value

fun Reader.makesDefault(bookshelf: Bookshelf): Answer<Unit> =
  libris.put(this, "/api/v1/me/default-bookshelf", mapOf("id" to bookshelf.id))

fun Reader.member(role: Role) =
  Member(id, displayName, role)

fun Bookshelf.withRole(role: Role) =
  ReaderBookshelf(id, name, role)
