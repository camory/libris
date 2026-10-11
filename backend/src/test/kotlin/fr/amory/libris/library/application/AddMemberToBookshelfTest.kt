package fr.amory.libris.library.application

import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import fr.amory.libris.library.application.AddMemberResult.Added
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AddMemberToBookshelfTest {
  private val marc = readerNamed("marc", "Marc")
  private val lea = readerNamed("lea", "Léa")
  private val familiale = Bookshelf(BookshelfId.new(), "Bibliothèque familiale", listOf(Membership(marc.id, OWNER)))
  private val bookshelves = BookshelvesInMemory().apply { insert(familiale) }
  private val readers = ReadersInMemory().apply {
    insert(marc)
    insert(lea)
  }
  private val transactions = TransactionsObserving { bookshelves.stored }
  private val addMemberToBookshelf = AddMemberToBookshelf(bookshelves, readers, transactions)

  @Test
  fun `an owner adds a reader, who becomes a member with the role given, stored in one transaction`() {
    // When
    val answer = addMemberToBookshelf(marc.id, familiale.id, Membership(lea.id, VIEWER))

    // Then
    val withLea = Bookshelf(
      familiale.id,
      "Bibliothèque familiale",
      listOf(Membership(marc.id, OWNER), Membership(lea.id, VIEWER)),
    )
    answer shouldBe Added(BookshelfMember(lea.id, "Léa", VIEWER))
    bookshelves.stored shouldBe listOf(withLea)
    transactions.recorded shouldBe listOf(Transaction(before = listOf(familiale), after = listOf(withLea)))
  }
}
