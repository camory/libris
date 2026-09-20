package fr.amory.libris.bibliography.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SeriesEntryTest {
    @Test
    fun `a series entry without a name is none`() {
        SeriesEntry.of(" ", 1) shouldBe null
        SeriesEntry.of(null, 1) shouldBe null
    }

    @Test
    fun `a series entry without a name is refused`() {
        shouldThrow<IllegalArgumentException> { SeriesEntry(" ", 1) }
    }
}
