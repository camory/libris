package fr.amory.libris.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

data class Bookshelf(
    val id: UUID = Generators.timeBasedEpochGenerator().generate(),
    val name: String,
    val members: List<Member>,
)
