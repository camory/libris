package fr.amory.libris.fixture

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.Reader
import java.util.UUID

fun readerOwning(bookshelf: Bookshelf, username: String, displayName: String, email: String = "$username@amory.fr") =
    Reader(username, email, displayName, bookshelf)

fun readerOwning(bookshelf: Bookshelf, id: UUID, username: String, displayName: String) =
    readerOwning(bookshelf, username, displayName).copy(id = id)
