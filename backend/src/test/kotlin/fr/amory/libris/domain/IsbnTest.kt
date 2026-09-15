package fr.amory.libris.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IsbnTest {
    @Test
    fun `an ISBN-13 exposes its thirteen digits`() {
        Isbn.of("9782723488525")?.digits shouldBe "9782723488525"
    }

    @Test
    fun `the last digit is the check digit of the first twelve`() {
        // Given / When / Then
        Isbn.of("9782000000006")?.digits shouldBe "9782000000006"
        Isbn.of("9791000000008")?.digits shouldBe "9791000000008"
        Isbn.of("9782723488526") shouldBe null
    }

    @Test
    fun `a 978 ISBN converts to the ten digits it was made from`() {
        // Given / When / Then
        Isbn.of("9782723488525")?.isbn10 shouldBe "2723488527"
        Isbn.of("9782253098058")?.isbn10 shouldBe "2253098051"
        Isbn.of("9780804429573")?.isbn10 shouldBe "080442957X"
    }

    @Test
    fun `an ISBN that does not start with 978 converts to no ten`() {
        Isbn.of("9791000000008")?.isbn10 shouldBe null
    }

    @Test
    fun `a text of another length is not an ISBN-13`() {
        // Given / When / Then
        Isbn.of("978272348852") shouldBe null
        Isbn.of("97827234885250") shouldBe null
    }

    @Test
    fun `a text that is not digits only is not an ISBN-13`() {
        // Given / When / Then
        Isbn.of("978-2-7234-8852-5") shouldBe null
        Isbn.of("9782723488525 ") shouldBe null
        Isbn.of("97827234885X5") shouldBe null
        Isbn.of("９７８２７２３４８８５２５") shouldBe null
        Isbn.of("") shouldBe null
    }
}
