package fr.amory.libris.application

import fr.amory.libris.domain.Bookshelf
import fr.amory.libris.domain.Member
import fr.amory.libris.domain.MemberRole.OWNER
import fr.amory.libris.fixture.BookshelvesInMemory
import fr.amory.libris.fixture.ReadersInMemory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FirstVisitTest {
    @Test
    fun `a reader Libris has never seen is welcomed with the bookshelf they own`() {
        // Given
        val readers = ReadersInMemory()
        val bookshelves = BookshelvesInMemory()
        val firstVisit = FirstVisit(readers, bookshelves)

        // When
        val lea = firstVisit.welcome("lea", "lea@amory.fr", "Léa")

        // Then
        bookshelves.stored shouldBe listOf(Bookshelf(id = lea.defaultBookshelfId, name = "Bibliothèque de Léa"))
        lea.memberships shouldBe listOf(Member(lea.defaultBookshelfId, OWNER))
        readers.findByUsername("lea") shouldBe lea
    }
}
