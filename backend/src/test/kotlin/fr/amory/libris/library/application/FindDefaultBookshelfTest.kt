package fr.amory.libris.library.application

import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FindDefaultBookshelfTest {
    @Test
    fun `a reader's default bookshelf is answered with its name`() {
        // Given
        val lea = readerNamed("lea", "Léa")
        val bookshelves = BookshelvesInMemory()
        bookshelves.insert(bookshelfOwnedBy(readerNamed("marc", "Marc")))
        bookshelves.insert(bookshelfOwnedBy(lea))
        val findDefaultBookshelf = FindDefaultBookshelf(bookshelves)

        // When
        val bookshelf = findDefaultBookshelf(lea)

        // Then
        bookshelf.id shouldBe lea.defaultBookshelfId
        bookshelf.name shouldBe "Bibliothèque de Léa"
    }
}
