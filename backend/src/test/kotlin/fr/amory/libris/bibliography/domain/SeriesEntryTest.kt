package fr.amory.libris.bibliography.domain

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class SeriesEntryTest {
    @Test
    fun `a series entry without a name is refused`() {
        shouldThrow<IllegalArgumentException> { SeriesEntry(" ", 1) }
    }
}
