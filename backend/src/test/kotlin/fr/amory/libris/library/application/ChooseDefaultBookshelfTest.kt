package fr.amory.libris.library.application

import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import fr.amory.libris.library.application.ChooseDefaultResult.Chosen
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ChooseDefaultBookshelfTest {
  private val lea = readerNamed("lea", "Léa")
  private val readers = ReadersInMemory().apply { insert(lea) }
  private val transactions = TransactionsObserving { readers.stored }
  private val chooseDefaultBookshelf = ChooseDefaultBookshelf(readers, transactions)

  @Test
  fun `an owner makes the bookshelf their default, stored in one transaction`() {
    // Given
    val salon = Bookshelf(BookshelfId.new(), BookshelfName("Salon"), listOf(Membership(lea.id, OWNER)))

    // When
    val answer = chooseDefaultBookshelf(lea, salon.id)

    // Then
    val withSalon = lea.copy(defaultBookshelfId = salon.id)
    answer shouldBe Chosen
    readers.stored shouldBe listOf(withSalon)
    transactions.recorded shouldBe listOf(Transaction(before = listOf(lea), after = listOf(withSalon)))
  }
}
