package fr.amory.libris.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MemberProfileTest {
    @Test
    fun `a new member profile is identified by a version 7 uuid`() {
        // Given, When
        val profile = MemberProfile("juliette", "Juliette")

        // Then
        profile.id.version() shouldBe 7
    }
}
