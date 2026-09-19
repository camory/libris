package fr.amory.libris.domain

import java.util.UUID

data class Member(
    val bookshelfId: UUID,
    val role: MemberRole,
)
