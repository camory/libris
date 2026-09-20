package fr.amory.libris.bibliography.domain

import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ContributionTest {
    @Test
    fun `a contribution without a name is none`() {
        Contribution.of(" ", WRITER) shouldBe null
        Contribution.of(null, WRITER) shouldBe null
    }

    @Test
    fun `a contribution without a name is refused`() {
        shouldThrow<IllegalArgumentException> { Contribution(" ", WRITER) }
    }
}
