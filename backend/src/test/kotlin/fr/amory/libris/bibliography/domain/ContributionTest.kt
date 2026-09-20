package fr.amory.libris.bibliography.domain

import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class ContributionTest {
    @Test
    fun `a contribution without a name is refused`() {
        shouldThrow<IllegalArgumentException> { Contribution(" ", WRITER) }
    }
}
