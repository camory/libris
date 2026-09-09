package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.MemberProfile
import fr.amory.libris.domain.MemberProfileRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class JdbcMemberProfileRepositoryTest @Autowired constructor(
    private val profiles: MemberProfileRepository,
) {
    @Test
    fun `an inserted member profile is found by its username`() {
        // Given
        val profile = MemberProfile("juliette", "Juliette")

        // When
        profiles.insert(profile)

        // Then
        profiles.findByUsername("juliette") shouldBe profile
    }

    @Test
    fun `an unknown username finds no member profile`() {
        // Given, When
        val found = profiles.findByUsername("nobody")

        // Then
        found.shouldBeNull()
    }

    @Test
    fun `a second member profile with the same username is refused`() {
        // Given
        profiles.insert(MemberProfile("juliette", "Juliette"))

        // When, Then
        shouldThrow<DuplicateKeyException> {
            profiles.insert(MemberProfile("juliette", "Juliette Amory"))
        }
    }
}
