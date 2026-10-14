package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.bookshelfOf
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class ListReaderBookshelvesTest {
  @Test
  fun `a reader's bookshelves are answered with their role on each, and none they are no member of`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val marc = readerNamed("marc", "Marc")
    val salon = bookshelfOf(
      BookshelfId.new(),
      BookshelfName("Salon"),
      listOf(Membership(marc.id, OWNER), Membership(lea.id, VIEWER)),
    )
    val bookshelves = BookshelvesInMemory()
    bookshelves.insert(bookshelfOwnedBy(lea))
    bookshelves.insert(salon)
    bookshelves.insert(bookshelfOwnedBy(marc))
    val listReaderBookshelves = ListReaderBookshelves(bookshelves)

    // When
    val answer = listReaderBookshelves(lea)

    // Then
    answer shouldContainExactlyInAnyOrder listOf(
      ReaderBookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", OWNER),
      ReaderBookshelf(salon.id, "Salon", VIEWER),
    )
  }
}
