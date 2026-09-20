package fr.amory.libris.library.domain.bookshelf

import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.reader.ReaderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BookshelfTest {
    @Test
    fun `the bookshelf of a reader is named after them and owned by them`() {
        // Given
        val lea = ReaderId.new()
        val id = BookshelfId.new()

        // When
        val bookshelf = Bookshelf.ownedBy(lea, "Léa", id)

        // Then
        bookshelf shouldBe Bookshelf(id, "Bibliothèque de Léa", listOf(Membership(lea, OWNER)))
    }

    @Test
    fun `a bookshelf without an owner is refused`() {
        shouldThrow<IllegalArgumentException> {
            Bookshelf(BookshelfId.new(), "Bibliothèque de personne", listOf(Membership(ReaderId.new(), VIEWER)))
        }
    }
}
