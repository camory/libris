package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.IdGenerator
import fr.amory.libris.domain.MemberProfile
import fr.amory.libris.domain.MemberProfileRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
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
    private val ids: IdGenerator,
) {
    @Test
    fun `inserts a profile and finds it back by username`() {
        // Given
        val id = ids.next()

        // When
        profiles.insert(MemberProfile(id = id, username = "juliette", displayName = "Juliette"))
        val found = profiles.findByUsername("juliette")

        // Then
        found.shouldNotBeNull()
        found.id shouldBe id
        found.displayName shouldBe "Juliette"
        found.createdAt.shouldNotBeNull()
        found.updatedAt.shouldNotBeNull()
    }

    @Test
    fun `refuses a second profile with an already used username`() {
        // Given
        profiles.insert(MemberProfile(id = ids.next(), username = "juliette", displayName = "Juliette"))

        // When, Then
        shouldThrow<DuplicateKeyException> {
            profiles.insert(MemberProfile(id = ids.next(), username = "juliette", displayName = "Juliette Bis"))
        }
    }
}
