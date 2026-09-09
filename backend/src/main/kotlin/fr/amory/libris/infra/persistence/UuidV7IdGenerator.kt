package fr.amory.libris.infra.persistence

import com.fasterxml.uuid.Generators
import java.util.UUID

class UuidV7IdGenerator {
    private val generator = Generators.timeBasedEpochGenerator()

    fun next(): UUID = generator.generate()
}
