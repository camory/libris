package fr.amory.libris.domain

enum class Role {
    READER,
    ADMIN,
}

data class Member(
    val username: String,
    val displayName: String,
    val email: String,
    val role: Role,
)
