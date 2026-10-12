package fr.amory.libris.library.application

import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import fr.amory.libris.library.application.ChooseDefaultResult.Chosen
import fr.amory.libris.library.application.ChooseDefaultResult.NoSuchBookshelf
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ChooseDefaultBookshelfTest {
  private val lea = readerNamed("lea", "Léa")
  private val marc = readerNamed("marc", "Marc")
  private val readers = ReadersInMemory().apply { insert(lea) }
  private val bookshelves = BookshelvesInMemory().apply {
    insert(bookshelfOwnedBy(lea))
    insert(bookshelfOwnedBy(marc))
  }
  private val transactions = TransactionsObserving { readers.stored }
  private val chooseDefaultBookshelf = ChooseDefaultBookshelf(readers, bookshelves, transactions)

  @Test
  fun `an owner makes the bookshelf their default, stored in one transaction`() {
    // Given
    val salon = Bookshelf(BookshelfId.new(), BookshelfName("Salon"), listOf(Membership(lea.id, OWNER)))
    bookshelves.insert(salon)

    // When
    val answer = chooseDefaultBookshelf(lea, salon.id)

    // Then
    val withSalon = lea.copy(defaultBookshelfId = salon.id)
    answer shouldBe Chosen
    readers.stored shouldBe listOf(withSalon)
    transactions.recorded shouldBe listOf(Transaction(before = listOf(lea), after = listOf(withSalon)))
  }

  @Test
  fun `a viewer of the bookshelf answers no such bookshelf, the reader as stored`() {
    // Given
    val salon = Bookshelf(
      BookshelfId.new(),
      BookshelfName("Salon"),
      listOf(Membership(marc.id, OWNER), Membership(lea.id, VIEWER)),
    )
    bookshelves.insert(salon)

    // When
    val answer = chooseDefaultBookshelf(lea, salon.id)

    // Then
    answer shouldBe NoSuchBookshelf
    readers.stored shouldBe listOf(lea)
    transactions.recorded shouldBe emptyList()
  }

  @Test
  fun `a reader who does not belong to the bookshelf answers no such bookshelf, the reader as stored`() {
    // Given
    val salon = Bookshelf(BookshelfId.new(), BookshelfName("Salon"), listOf(Membership(marc.id, OWNER)))
    bookshelves.insert(salon)

    // When
    val answer = chooseDefaultBookshelf(lea, salon.id)

    // Then
    answer shouldBe NoSuchBookshelf
    readers.stored shouldBe listOf(lea)
    transactions.recorded shouldBe emptyList()
  }

  @Test
  fun `an id naming no bookshelf answers no such bookshelf, the reader as stored`() {
    // When
    val answer = chooseDefaultBookshelf(lea, BookshelfId.new())

    // Then
    answer shouldBe NoSuchBookshelf
    readers.stored shouldBe listOf(lea)
    transactions.recorded shouldBe emptyList()
  }
}
