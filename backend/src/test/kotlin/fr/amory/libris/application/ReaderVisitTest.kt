package fr.amory.libris.application

import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import fr.amory.libris.fixture.BookshelvesInMemory
import fr.amory.libris.fixture.ReadersInMemory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class ReaderVisitTest {
    @Test
    fun `a first visit stores the reader of the headers`() {
        // Given
        val readers = ReadersInMemory()
        val visit = ReaderVisit(readers, FirstVisit(readers, BookshelvesInMemory()))

        // When
        val juliette = visit.visit("juliette", "juliette@amory.fr", "Juliette")

        // Then
        juliette.username shouldBe "juliette"
        juliette.email shouldBe "juliette@amory.fr"
        juliette.displayName shouldBe "Juliette"
        readers.findByUsername("juliette") shouldBe juliette
    }

    @Test
    fun `a later visit returns the stored reader and keeps the display name Libris owns`() {
        // Given
        val readers = ReadersInMemory()
        val visit = ReaderVisit(readers, FirstVisit(readers, BookshelvesInMemory()))
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
        val visit = ReaderVisit(readers, FirstVisit(readers, bookshelves))
        readers.insert(
            Reader(
                username = "juliette",
                email = "juliette@amory.fr",
                displayName = "Juliette",
                defaultBookshelfId = UUID.randomUUID(),
            ),
        )

        // When
        visit.visit("juliette", "juliette@amory.fr", "Juliette")

        // Then
        bookshelves.stored shouldBe emptyList()
    }

    @Test
    fun `a first visit that loses the race to another one returns the reader it stored`() {
        // Given
        val winner = Reader(
            username = "juliette",
            email = "juliette@amory.fr",
            displayName = "Juliette",
            defaultBookshelfId = UUID.randomUUID(),
        )
        val readers = ReadersLosingTheRace(winner)
        val visit = ReaderVisit(readers, FirstVisit(readers, BookshelvesInMemory()))

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
