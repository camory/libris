package fr.amory.libris.bibliography.domain.cover

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

private const val NAME = "3f7a9c0e5b2d4816a0c9e7f1b3d5a2c4e6f8091b2c3d4e5f60718293a4b5c6d7"

class CoverNameTest {
    @Test
    fun `sixty-four lower-case hexadecimal digits name a cover`() {
        // Given / When / Then
        CoverName.of(NAME)?.value shouldBe NAME
    }

    @Test
    fun `what is not sixty-four lower-case hexadecimal digits names no cover`() {
        // Given / When / Then
        CoverName.of(NAME.drop(1)) shouldBe null
        CoverName.of(NAME + "0") shouldBe null
        CoverName.of("A" + NAME.drop(1)) shouldBe null
        CoverName.of("../" + NAME.drop(3)) shouldBe null
    }
}
