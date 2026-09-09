package fr.amory.libris.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

private val uuids = Generators.timeBasedEpochGenerator()

data class MemberProfile(
    val username: String,
    val displayName: String,
    val id: UUID = uuids.generate(),
)
