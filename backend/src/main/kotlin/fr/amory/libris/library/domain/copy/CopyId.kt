package fr.amory.libris.library.domain.copy

import com.fasterxml.uuid.Generators
import java.util.UUID

@JvmInline
value class CopyId(val value: UUID) {
    companion object {
        fun new(): CopyId = CopyId(Generators.timeBasedEpochGenerator().generate())
    }
}
