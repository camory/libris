package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository
import fr.amory.libris.library.fixture.BookshelvesInMemory
import fr.amory.libris.library.fixture.ReadersInMemory
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionOperations.withoutTransaction

class ReaderVisitTest {
    @Test
    fun `a first visit stores the reader of the headers`() {
        // Given
        val readers = ReadersInMemory()
        val visit = ReaderVisit(readers, BookshelvesInMemory(), withoutTransaction())

        // When
        val juliette = visit.visit("juliette", "juliette@amory.fr", "Juliette")

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
        val visit = ReaderVisit(ReadersInMemory(), bookshelves, withoutTransaction())

        // When
        val lea = visit.visit("lea", "lea@amory.fr", "Léa")

        // Then
        val bookshelf = bookshelves.stored.single()
        bookshelf.name shouldBe "Bibliothèque de Léa"
        bookshelf.memberships shouldBe listOf(Membership(lea.id, OWNER))
    }

    @Test
    fun `a later visit returns the stored reader and keeps the display name Libris owns`() {
        // Given
        val visit = ReaderVisit(ReadersInMemory(), BookshelvesInMemory(), withoutTransaction())
        val firstVisit = visit.visit("juliette", "juliette@amory.fr", "Juliette")

        // When
        val laterVisit = visit.visit("juliette", "juju@amory.fr", "Juju")

        // Then
        laterVisit shouldBe firstVisit
    }

    @Test
    fun `a visit of a reader Libris knows creates no bookshelf`() {
        // Given
        val readers = ReadersInMemory()
        val bookshelves = BookshelvesInMemory()
        val visit = ReaderVisit(readers, bookshelves, withoutTransaction())
        readers.insert(readerNamed("juliette", "Juliette"))

        // When
        visit.visit("juliette", "juliette@amory.fr", "Juliette")

        // Then
        bookshelves.stored shouldBe emptyList()
    }

    @Test
    fun `a first visit that loses the race to another one returns the reader it stored`() {
        // Given
        val winner = readerNamed("juliette", "Juliette")
        val readers = ReadersLosingTheRace(winner)
        val visit = ReaderVisit(readers, BookshelvesInMemory(), withoutTransaction())

        // When
        val loser = visit.visit("juliette", "juju@amory.fr", "Juju")

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
