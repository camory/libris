package fr.amory.libris.library.application

import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import fr.amory.libris.library.application.CreateBookshelfResult.Created
import fr.amory.libris.library.application.CreateBookshelfResult.NameTaken
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.BookshelvesInMemory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class CreateBookshelfTest {
  @Test
  fun `a created bookshelf bears the name asked, the reader its one owner, stored in one transaction`() {
    // Given
    val lea = ReaderId.new()
    val bookshelves = BookshelvesInMemory()
    val transactions = TransactionsObserving { bookshelves.stored }
    val createBookshelf = CreateBookshelf(bookshelves, transactions)

    // When
    val answer = createBookshelf(lea, BookshelfName("Bibliothèque familiale"))

    // Then
    val created = answer.shouldBeInstanceOf<Created>().bookshelf
    created.name shouldBe BookshelfName("Bibliothèque familiale")
    created.memberships shouldBe listOf(Membership(lea, OWNER))
    bookshelves.stored shouldBe listOf(created)
    transactions.recorded shouldBe listOf(Transaction(before = emptyList<Bookshelf>(), after = listOf(created)))
  }

  @Test
  fun `a name the reader has among their bookshelves, even as a viewer, ignoring case and accents, is taken`() {
    // Given
    val marc = ReaderId.new()
    val lea = ReaderId.new()
    val familiale = Bookshelf(
      BookshelfId.new(),
      BookshelfName("Bibliothèque familiale"),
      listOf(Membership(marc, OWNER), Membership(lea, VIEWER)),
    )
    val bookshelves = BookshelvesInMemory().apply { insert(familiale) }
    val transactions = TransactionsObserving { bookshelves.stored }
    val createBookshelf = CreateBookshelf(bookshelves, transactions)

    // When
    val answer = createBookshelf(lea, BookshelfName("bibliotheque FAMILIALE"))

    // Then
    answer shouldBe NameTaken
    bookshelves.stored shouldBe listOf(familiale)
    transactions.recorded shouldBe emptyList()
  }
}
