package fr.amory.libris.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class Isbn13Test {
    @Test
    fun `an ISBN-13 exposes its thirteen digits`() {
        Isbn13.of("9782723488525")?.digits shouldBe "9782723488525"
    }
}
