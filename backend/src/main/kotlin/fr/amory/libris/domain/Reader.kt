package fr.amory.libris.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

data class Reader(
    val id: UUID = Generators.timeBasedEpochGenerator().generate(),
    val username: String,
    val email: String,
    val displayName: String,
)
