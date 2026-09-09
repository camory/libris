package fr.amory.libris.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("member")
data class MemberProfile(
    @Id val id: UUID,
    val username: String,
    val displayName: String,
    @CreatedDate val createdAt: Instant? = null,
    @LastModifiedDate val updatedAt: Instant? = null,
)
