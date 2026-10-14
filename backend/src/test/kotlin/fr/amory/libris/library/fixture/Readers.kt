package fr.amory.libris.library.fixture

import fr.amory.libris.fixture.MutableClock
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import java.time.Instant

fun readerNamed(
  username: String,
  displayName: String,
  email: String = "$username@amory.fr",
  id: ReaderId = ReaderId.new(),
  defaultBookshelfId: BookshelfId = BookshelfId.new()): Reader =
  Reader(id, username, email, displayName, defaultBookshelfId)

fun bookshelfOwnedBy(reader: Reader, at: Instant = MutableClock().instant()): Bookshelf =
  Bookshelf.ownedBy(reader.id, reader.displayName, reader.defaultBookshelfId, at)

fun bookshelfOf(id: BookshelfId, name: BookshelfName, memberships: List<Membership>): Bookshelf =
  Bookshelf(id, name, memberships, memberships.first().readerId, MutableClock().instant())
