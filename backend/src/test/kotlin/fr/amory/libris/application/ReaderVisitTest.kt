package fr.amory.libris.application

import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ReaderVisitTest {
    @Test
    fun `a first visit stores the reader of the headers`() {
        // Given
        val readers = ReadersInMemory()
        val visit = ReaderVisit(readers)

        // When
        val juliette = visit.visit("juliette", "juliette@amory.fr", "Juliette")

        // Then
        juliette.username shouldBe "juliette"
        juliette.email shouldBe "juliette@amory.fr"
        juliette.displayName shouldBe "Juliette"
        readers.findByUsername("juliette") shouldBe juliette
    }
}

private class ReadersInMemory : ReaderRepository {
    private val stored = mutableMapOf<String, Reader>()

    override fun insert(reader: Reader) {
        stored[reader.username] = reader
    }

    override fun findByUsername(username: String): Reader? = stored[username]
}
