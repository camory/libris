package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.Transaction
import fr.amory.libris.library.fixture.TransactionsObserving
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
        val welcomeReader = WelcomeReader(readers, BookshelvesInMemory(), withoutTransaction())

        // When
        val juliette = welcomeReader("juliette", "juliette@amory.fr", "Juliette")

        // Then
        juliette.username shouldBe "juliette"
        juliette.email shouldBe "juliette@amory.fr"
        juliette.displayName shouldBe "Juliette"
        readers.findByUsername("juliette") shouldBe juliette
    }

    @Test
    fun `a first visit creates the bookshelf the reader owns, their default`() {
        // Given
        val bookshelves = BookshelvesInMemory()
        val welcomeReader = WelcomeReader(ReadersInMemory(), bookshelves, withoutTransaction())

        // When
        val lea = welcomeReader("lea", "lea@amory.fr", "Léa")

        // Then
        bookshelves.stored shouldBe listOf(
            Bookshelf(lea.defaultBookshelfId, "Bibliothèque de Léa", listOf(Membership(lea.id, OWNER))),
        )
    }

    @Test
    fun `a first visit stores the reader and their bookshelf in one transaction`() {
        // Given
        val readers = ReadersInMemory()
        val bookshelves = BookshelvesInMemory()
        val transactions = TransactionsObserving { readers.stored to bookshelves.stored }
        val welcomeReader = WelcomeReader(readers, bookshelves, transactions)

        // When
        val lea = welcomeReader("lea", "lea@amory.fr", "Léa")

        // Then
        transactions.recorded shouldBe listOf(
            Transaction(
                before = emptyList<Reader>() to emptyList<Bookshelf>(),
                after = listOf(lea) to listOf(bookshelfOwnedBy(lea)),
            ),
        )
    }

    @Test
    fun `a later visit returns the stored reader and keeps the display name Libris owns`() {
        // Given
        val welcomeReader = WelcomeReader(ReadersInMemory(), BookshelvesInMemory(), withoutTransaction())
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
        val welcomeReader = WelcomeReader(readers, bookshelves, withoutTransaction())
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
        val welcomeReader = WelcomeReader(readers, BookshelvesInMemory(), withoutTransaction())

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

    override fun findByUsername(username: String): Reader? = winner.takeIf { raceLost }
}
