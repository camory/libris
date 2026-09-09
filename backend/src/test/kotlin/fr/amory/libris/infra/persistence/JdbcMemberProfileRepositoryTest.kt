package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.MemberProfile
import fr.amory.libris.domain.MemberProfileRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
}
