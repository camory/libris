package fr.amory.libris.fixture

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole.OWNER
import fr.amory.libris.domain.Reader
import java.util.UUID

fun readerOwning(bookshelf: Bookshelf, username: String, displayName: String, email: String = "$username@amory.fr") =
    Reader(
        username = username,
        email = email,
        displayName = displayName,
        memberships = listOf(Member(bookshelf.id, OWNER)),
        defaultBookshelfId = bookshelf.id,
    )

fun readerOwning(bookshelf: Bookshelf, id: UUID, username: String, displayName: String) =
    readerOwning(bookshelf, username, displayName).copy(id = id)
