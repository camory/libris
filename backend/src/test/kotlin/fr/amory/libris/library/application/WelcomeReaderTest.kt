package fr.amory.libris.library.application

import fr.amory.libris.fixture.MutableClock
import fr.amory.libris.fixture.Transaction
import fr.amory.libris.fixture.TransactionsObserving
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

class WelcomeReaderTest {
  @Test
  fun `a first visit stores the reader of the headers`() {
    // Given
    val readers = ReadersInMemory()
    val welcomeReader = WelcomeReader(readers, BookshelvesInMemory(), withoutTransaction(), MutableClock())

    // When
    val juliette = welcomeReader("juliette", "juliette@amory.fr", "Juliette")

    // Then
    juliette.username shouldBe "juliette"
    juliette.email shouldBe "juliette@amory.fr"
    juliette.displayName shouldBe "Juliette"
    readers.findByUsername("juliette") shouldBe juliette
  }

  @Test
  fun `a first visit creates the bookshelf the reader owns and created, now, their default`() {
    // Given
    val clock = MutableClock()
    val bookshelves = BookshelvesInMemory()
    val welcomeReader = WelcomeReader(ReadersInMemory(), bookshelves, withoutTransaction(), clock)

    // When
    val lea = welcomeReader("lea", "lea@amory.fr", "Léa")

    // Then
    bookshelves.stored shouldBe listOf(
      Bookshelf(
        lea.defaultBookshelfId,
        BookshelfName("Bibliothèque de Léa"),
        listOf(Membership(lea.id, OWNER)),
        lea.id,
        clock.instant(),
      ),
    )
  }

  @Test
  fun `a first visit stores the reader and their bookshelf in one transaction`() {
    // Given
    val readers = ReadersInMemory()
    val bookshelves = BookshelvesInMemory()
    val clock = MutableClock()
    val transactions = TransactionsObserving { readers.stored to bookshelves.stored }
    val welcomeReader = WelcomeReader(readers, bookshelves, transactions, clock)

    // When
    val lea = welcomeReader("lea", "lea@amory.fr", "Léa")

    // Then
    transactions.recorded shouldBe listOf(
      Transaction(
        before = emptyList<Reader>() to emptyList<Bookshelf>(),
        after = listOf(lea) to listOf(bookshelfOwnedBy(lea, clock.instant())),
      ),
    )
  }

  @Test
  fun `a later visit returns the stored reader and keeps the display name Libris owns`() {
    // Given
    val welcomeReader = WelcomeReader(ReadersInMemory(), BookshelvesInMemory(), withoutTransaction(), MutableClock())
    val firstVisit = welcomeReader("juliette", "juliette@amory.fr", "Juliette")

    // When
    val laterVisit = welcomeReader("juliette", "juju@amory.fr", "Juju")

    // Then
    laterVisit shouldBe firstVisit
  }

  @Test
  fun `a visit of a reader Libris knows creates no bookshelf`() {
    // Given
    val readers = ReadersInMemory()
    val bookshelves = BookshelvesInMemory()
    val welcomeReader = WelcomeReader(readers, bookshelves, withoutTransaction(), MutableClock())
    readers.insert(readerNamed("juliette", "Juliette"))

    // When
    welcomeReader("juliette", "juliette@amory.fr", "Juliette")

    // Then
    bookshelves.stored shouldBe emptyList()
  }

  @Test
  fun `a first visit that loses the race to another one returns the reader it stored`() {
    // Given
    val winner = readerNamed("juliette", "Juliette")
    val readers = ReadersLosingTheRace(winner)
    val welcomeReader = WelcomeReader(readers, BookshelvesInMemory(), withoutTransaction(), MutableClock())

    // When
    val loser = welcomeReader("juliette", "juju@amory.fr", "Juju")

    // Then
    loser shouldBe winner
  }
}

private class ReadersLosingTheRace(private val winner: Reader) : ReaderRepository {
  private var raceLost = false

  override fun insert(reader: Reader) {
    raceLost = true
    throw DuplicateUsernameException(reader.username)
  }

  override fun update(reader: Reader) =
    throw UnsupportedOperationException()

  override fun findByUsername(username: String): Reader? =
    winner.takeIf { raceLost }

  override fun findById(id: ReaderId): Reader? =
    winner.takeIf { raceLost && it.id == id }

  override fun findByIds(ids: List<ReaderId>): List<Reader> =
    listOfNotNull(winner.takeIf { raceLost && it.id in ids })

  override fun findAll(): List<Reader> =
    listOfNotNull(winner.takeIf { raceLost })
}
