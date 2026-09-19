package fr.amory.libris.domain

import java.util.UUID

data class Member(
    val readerId: UUID,
    val role: MemberRole,
)
