package fr.amory.libris.infra.persistence

import com.fasterxml.uuid.Generators
import fr.amory.libris.domain.IdGenerator
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidV7IdGenerator : IdGenerator {
    private val generator = Generators.timeBasedEpochGenerator()

    override fun next(): UUID = generator.generate()
}
