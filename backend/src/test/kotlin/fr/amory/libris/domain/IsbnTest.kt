package fr.amory.libris.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IsbnTest {
    @Test
    fun `an ISBN exposes its thirteen digits`() {
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
    fun `hyphens and spaces are dropped wherever they sit`() {
        // Given / When / Then
        Isbn.of("978-2-7234-8852-5")?.digits shouldBe "9782723488525"
        Isbn.of("978 2 7234 8852 5")?.digits shouldBe "9782723488525"
    }

    @Test
    fun `a thirteen-digit EAN with another prefix is not an ISBN`() {
        // Given / When / Then
        Isbn.of("4006381333931") shouldBe null
    }

    @Test
    fun `an ISBN-10 is converted to the thirteen digits it became`() {
        // Given / When / Then
        Isbn.of("2723488527")?.digits shouldBe "9782723488525"
        Isbn.of("2-7234-8852-7")?.digits shouldBe "9782723488525"
    }

    @Test
    fun `an ISBN-10 whose check character is X is converted, in either case`() {
        // Given / When / Then
        Isbn.of("080442957X")?.digits shouldBe "9780804429573"
        Isbn.of("080442957x")?.digits shouldBe "9780804429573"
    }

    @Test
    fun `an ISBN-10 whose check character is wrong is not an ISBN`() {
        Isbn.of("2723488521") shouldBe null
    }

    @Test
    fun `an ISBN-10 whose check character is neither a digit nor X is not an ISBN`() {
        // Given / When / Then
        Isbn.of("000000000\t") shouldBe null
        Isbn.of("000000000\n") shouldBe null
        Isbn.of("000000000\u00a0") shouldBe null
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
    fun `a text of another length is not an ISBN`() {
        // Given / When / Then
        Isbn.of("978272348852") shouldBe null
        Isbn.of("97827234885250") shouldBe null
    }

    @Test
    fun `a text that is not digits only is not an ISBN`() {
        // Given / When / Then
        Isbn.of("97827234885X5") shouldBe null
        Isbn.of("978-2-7234-8852-X") shouldBe null
        Isbn.of("９７８２７２３４８８５２５") shouldBe null
        Isbn.of("") shouldBe null
    }
}
