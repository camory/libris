package fr.amory.libris.library.domain.reader

import com.fasterxml.uuid.Generators
import java.util.UUID

@JvmInline
value class ReaderId(val value: UUID) {
    companion object {
        fun new(): ReaderId = ReaderId(Generators.timeBasedEpochGenerator().generate())
    }
}
