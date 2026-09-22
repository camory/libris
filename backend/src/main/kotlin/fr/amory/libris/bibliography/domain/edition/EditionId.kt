package fr.amory.libris.bibliography.domain.edition

import com.fasterxml.uuid.Generators
import java.util.UUID

@JvmInline
value class EditionId(val value: UUID) {
    companion object {
        fun new(): EditionId = EditionId(Generators.timeBasedEpochGenerator().generate())
    }
}
