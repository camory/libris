package fr.amory.libris.infra.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.fixture.OpenLibraryStubs
import fr.amory.libris.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class OpenLibrarySourceTest {
    private val source = OpenLibrarySource(server.baseUrl(), TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        server.resetAll()
    }

    @Test
    fun `a known ISBN is what Open Library knows about it`() {
        // Given
        openLibrary.knows(SPACE_WARS)

        // When
        val answer = source.lookUp(isbnOf(SPACE_WARS))

        // Then
        answer shouldBe Known(
            SourceEdition(
                isbn = isbnOf(SPACE_WARS),
                title = "Space Wars - Chapitre 1",
                subtitle = null,
                authors = listOf(
                    SourceAuthor("Baba", WRITER),
                    SourceAuthor("Stéphane Lapuss'", WRITER),
                    SourceAuthor("Tartuff", WRITER),
                ),
                series = null,
                collection = null,
                publisher = "KENNES EDITIONS",
                publicationYear = 2020,
                language = null,
                pageCount = 64,
                summary = null,
                coverUrl = "https://covers.openlibrary.org/b/isbn/9782380751673-L.jpg",
            ),
        )
    }

    private companion object {
        const val SPACE_WARS = "9782380751673"
        val TIMEOUT: Duration = ofMillis(200)
        val WARM_UP_TIMEOUT: Duration = ofSeconds(20)
        val server = WireMockServer(options().dynamicPort())
        val openLibrary = OpenLibraryStubs(server)

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            server.start()
            openLibrary.knows(SPACE_WARS)
            OpenLibrarySource(server.baseUrl(), WARM_UP_TIMEOUT).lookUp(isbnOf(SPACE_WARS))
            server.resetAll()
        }

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            server.stop()
        }
    }
}
