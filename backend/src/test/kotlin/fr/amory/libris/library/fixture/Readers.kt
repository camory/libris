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
): Reader = Reader(id, username, email, displayName)

fun bookshelfOwnedBy(reader: Reader, id: BookshelfId = BookshelfId.new()): Bookshelf =
    Bookshelf.ownedBy(reader.id, reader.displayName, id)
