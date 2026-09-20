package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.reader.ReaderId

data class Membership(
    val readerId: ReaderId,
    val role: MembershipRole,
)
