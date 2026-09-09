package fr.amory.libris.infra.persistence

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class UuidV7IdGeneratorTest {
    @Test
    fun `the generator answers a version 7 uuid`() {
        // Given
        val generator = UuidV7IdGenerator()

        // When
        val id = generator.next()

        // Then
        id.version() shouldBe 7
    }
}
