package fr.amory.libris.domain

import fr.amory.libris.domain.MemberRole.OWNER
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class ReaderTest {
    @Test
    fun `a reader created with a bookshelf owns it and has it as their default`() {
        // Given
        val bookshelf = Bookshelf(name = "Bibliothèque de Léa")

        // When
        val lea = Reader(username = "lea", email = "lea@amory.fr", displayName = "Léa", bookshelf = bookshelf)

        // Then
        lea.memberships shouldBe listOf(Member(bookshelf.id, OWNER))
        lea.defaultBookshelfId shouldBe bookshelf.id
    }

    @Test
    fun `a reader defaulting to a bookshelf they are no member of is refused`() {
        // Given
        val theirs = Bookshelf(name = "Bibliothèque de Léa")
        val another = UUID.randomUUID()

        // When, Then
        shouldThrow<IllegalArgumentException> {
            Reader(
                username = "lea",
                email = "lea@amory.fr",
                displayName = "Léa",
                memberships = listOf(Member(theirs.id, OWNER)),
                defaultBookshelfId = another,
            )
        }
    }
}
