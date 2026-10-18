package fr.amory.libris.library.application

import fr.amory.libris.bibliography.application.edition.HoldEdition
import fr.amory.libris.bibliography.application.edition.NewEdition
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.fixture.AwaitedCoversInMemory
import fr.amory.libris.bibliography.fixture.CoverWorkerObserving
import fr.amory.libris.bibliography.fixture.EditionsInMemory
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.MutableClock
import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.bookshelf.MembershipRole.VIEWER
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.CopiesInMemory
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

private const val ONE_PIECE = "9782723488525"

class AddBookToBookshelfTest {
  private val editions = EditionsInMemory()
  private val awaitedCovers = AwaitedCoversInMemory()
  private val copies = CopiesInMemory()
  private val bookshelves = BookshelvesInMemory()
  private val coverWorker = CoverWorkerObserving {}
  private val clock = MutableClock()
  private val holdEdition = HoldEdition(editions, awaitedCovers, withoutTransaction())
  private val addBookToBookshelf =
    AddBookToBookshelf(holdEdition, copies, bookshelves, withoutTransaction(), coverWorker, clock)

  @Test
  fun `the house lacking the ISBN gains the edition and the copy, added by the reader, now`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val bookshelf = bookshelfOwnedBy(lea)
    bookshelves.insert(bookshelf)

    // When
    val result = addBookToBookshelf(lea.id, bookshelf.id, onePieceTomeOne())

    // Then
    val edition = editions.stored.single()
    val copy = copies.stored.single()
    copy shouldBe Copy(copy.id, edition.id, bookshelf.id, lea.id, clock.instant())
    result shouldBe Added(copy, bookshelf)
  }

  @Test
  fun `a copy an owner adds to a bookshelf another reader created is added by that owner`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val juliette = readerNamed("juliette", "Juliette")
    val bookshelf = bookshelfOwnedBy(lea).withMember(Membership(juliette.id, OWNER))
    bookshelves.insert(bookshelf)

    // When
    addBookToBookshelf(juliette.id, bookshelf.id, onePieceTomeOne())

    // Then
    copies.stored.single().addedBy shouldBe juliette.id
  }

  @Test
  fun `the same reader adding it again puts a second copy beside the first`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val bookshelf = bookshelfOwnedBy(lea)
    bookshelves.insert(bookshelf)
    addBookToBookshelf(lea.id, bookshelf.id, onePieceTomeOne())

    // When
    addBookToBookshelf(lea.id, bookshelf.id, onePieceTomeOne())

    // Then
    val edition = editions.stored.single()
    val (first, second) = copies.stored
    first shouldBe Copy(first.id, edition.id, bookshelf.id, lea.id, clock.instant())
    second shouldBe Copy(second.id, edition.id, bookshelf.id, lea.id, clock.instant())
    second.id shouldNotBe first.id
  }

  @Test
  fun `a bookshelf id no bookshelf carries is refused`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val unknown = bookshelfOwnedBy(lea)

    // When
    val result = addBookToBookshelf(lea.id, unknown.id, onePieceTomeOne())

    // Then
    result shouldBe NoSuchBookshelf
    editions.stored.shouldBeEmpty()
    copies.stored.shouldBeEmpty()
  }

  @Test
  fun `a bookshelf the reader is not a member of is refused`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val leasBookshelf = bookshelfOwnedBy(lea)
    bookshelves.insert(leasBookshelf)
    val juliette = readerNamed("juliette", "Juliette")

    // When
    val result = addBookToBookshelf(juliette.id, leasBookshelf.id, onePieceTomeOne())

    // Then
    result shouldBe NoSuchBookshelf
    editions.stored.shouldBeEmpty()
    copies.stored.shouldBeEmpty()
  }

  @Test
  fun `a viewer of the bookshelf cannot add to it`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val juliette = readerNamed("juliette", "Juliette")
    val leasBookshelf = bookshelfOwnedBy(lea).let {
      it.copy(memberships = it.memberships + Membership(juliette.id, VIEWER))
    }
    bookshelves.insert(leasBookshelf)

    // When
    val result = addBookToBookshelf(juliette.id, leasBookshelf.id, onePieceTomeOne())

    // Then
    result shouldBe NotAnOwner
    editions.stored.shouldBeEmpty()
    copies.stored.shouldBeEmpty()
  }

  @Test
  fun `the edition, its awaited cover and the copy land in one transaction`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val bookshelf = bookshelfOwnedBy(lea)
    bookshelves.insert(bookshelf)
    val transactions = TransactionsObserving {
      Triple(editions.stored.size, awaitedCovers.stored.size, copies.stored.size)
    }
    val addBookToBookshelf =
      AddBookToBookshelf(holdEdition, copies, bookshelves, transactions, coverWorker, clock)

    // When
    addBookToBookshelf(lea.id, bookshelf.id, onePieceTomeOne())

    // Then
    transactions.recorded shouldBe listOf(Transaction(before = Triple(0, 0, 0), after = Triple(1, 1, 1)))
  }

  @Test
  fun `an added ouvrage wakes the worker`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val bookshelf = bookshelfOwnedBy(lea)
    bookshelves.insert(bookshelf)

    // When
    addBookToBookshelf(lea.id, bookshelf.id, onePieceTomeOne())

    // Then
    coverWorker.wakings.size shouldBe 1
  }

  @Test
  fun `the worker is woken after the add's transaction`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val bookshelf = bookshelfOwnedBy(lea)
    bookshelves.insert(bookshelf)
    val transactions = TransactionsObserving {}
    val coverWorker = CoverWorkerObserving { transactions.recorded.size }
    val addBookToBookshelf =
      AddBookToBookshelf(holdEdition, copies, bookshelves, transactions, coverWorker, clock)

    // When
    addBookToBookshelf(lea.id, bookshelf.id, onePieceTomeOne())

    // Then
    coverWorker.wakings shouldBe listOf(1)
  }

  @Test
  fun `a refused add wakes no worker`() {
    // Given
    val lea = readerNamed("lea", "Léa")
    val juliette = readerNamed("juliette", "Juliette")
    val leasBookshelf = bookshelfOwnedBy(lea).let {
      it.copy(memberships = it.memberships + Membership(juliette.id, VIEWER))
    }
    bookshelves.insert(leasBookshelf)

    // When
    val result = addBookToBookshelf(juliette.id, leasBookshelf.id, onePieceTomeOne())

    // Then
    result shouldBe NotAnOwner
    coverWorker.wakings.shouldBeEmpty()
  }

  private fun onePieceTomeOne(): NewEdition =
    NewEdition(
      isbn = isbnOf(ONE_PIECE),
      kind = MANGA,
      title = "Romance dawn",
      subtitle = "À l'aube d'une grande aventure",
      contributions = Contributions.of(listOf(Contribution("Eiichiro Oda", WRITER))),
      series = SeriesEntry("One piece", 1),
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 208,
      summary = "Luffy prend la mer pour devenir le roi des pirates.",
      coverSource = null,
    )
}
