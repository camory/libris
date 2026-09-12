package fr.amory.libris.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class Isbn13Test {
    @Test
    fun `an ISBN-13 exposes its thirteen digits`() {
        Isbn13.of("9782723488525")?.digits shouldBe "9782723488525"
    }

    @Test
    fun `the last digit is the check digit of the first twelve`() {
        // Given / When / Then
        Isbn13.of("9782000000006")?.digits shouldBe "9782000000006"
        Isbn13.of("9791000000008")?.digits shouldBe "9791000000008"
        Isbn13.of("9782723488526") shouldBe null
    }
}
