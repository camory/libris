package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId

fun readerNamed(
    username: String,
    displayName: String,
    email: String = "$username@amory.fr",
    id: ReaderId = ReaderId.new(),
    defaultBookshelfId: BookshelfId = BookshelfId.new(),
): Reader = Reader(id, username, email, displayName, defaultBookshelfId)

fun bookshelfOwnedBy(reader: Reader, name: String = "Bibliothèque de ${reader.displayName}"): Bookshelf =
    Bookshelf(reader.defaultBookshelfId, name, listOf(Membership(reader.id, OWNER)))
