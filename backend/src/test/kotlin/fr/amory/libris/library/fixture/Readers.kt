package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId

fun readerNamed(
    username: String,
    displayName: String,
    email: String = "$username@amory.fr",
    id: ReaderId = ReaderId.new(),
    defaultBookshelfId: BookshelfId = BookshelfId.new(),
): Reader = Reader(id, username, email, displayName, defaultBookshelfId)

fun bookshelfOwnedBy(reader: Reader): Bookshelf =
    Bookshelf.ownedBy(reader.id, reader.displayName, reader.defaultBookshelfId)
