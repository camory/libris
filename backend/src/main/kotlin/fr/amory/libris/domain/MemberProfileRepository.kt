package fr.amory.libris.domain

interface MemberProfileRepository {
    fun insert(profile: MemberProfile): MemberProfile

    fun findByUsername(username: String): MemberProfile?
}
