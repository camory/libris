package fr.amory.libris.infra.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.fixture.OpenLibraryStubs
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration

class OpenLibrarySourceTest {
    private val source = OpenLibrarySource(server.baseUrl(), TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        server.resetAll()
    }

    @Test
    fun `the source names itself`() {
        source.source shouldBe Source.OPEN_LIBRARY
    }

    @Test
    fun `a known ISBN is what Open Library knows about it`() {
        // Given
        openLibrary.knows(ONE_PIECE)

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.Known(
            SourceEdition(
                isbn13 = isbn(ONE_PIECE),
                title = "One Piece - Édition originale Tome 01",
                subtitle = "Romamce Dawn - À l'aube d'une grande aventure",
                authors = listOf(SourceAuthor("尾田栄一郎", WRITER), SourceAuthor("Shueisha", WRITER)),
                series = null,
                collection = null,
                publisher = "Shônen Manga",
                publicationYear = 2013,
                language = null,
                pageCount = 207,
                summary = null,
                coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
            ),
        )
    }

    @Test
    fun `looking up an ISBN asks for the edition and its authors, never for the cover`() {
        // Given
        openLibrary.knows(ONE_PIECE)

        // When
        source.lookUp(isbn(ONE_PIECE))

        // Then
        server.allServeEvents.map { it.request.url } shouldContainExactlyInAnyOrder listOf(
            "/isbn/$ONE_PIECE.json",
            "/books/OL33773404M.json",
            "/authors/OL2733294A.json",
            "/authors/OL7476994A.json",
        )
    }

    @Test
    fun `an ISBN Open Library does not know is nothing known`() {
        // Given
        openLibrary.doesNotKnow(ONE_PIECE)

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.NothingKnown
    }

    @Test
    fun `an Open Library that fails on the edition is a failure`() {
        // Given
        openLibrary.fails()

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.Failed
    }

    @Test
    fun `an Open Library that fails on an author is a failure`() {
        // Given
        openLibrary.knows(ONE_PIECE)
        openLibrary.failsOn("/authors/OL2733294A.json")

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.Failed
    }

    @Test
    fun `an Open Library whose author has no name is a failure`() {
        // Given
        openLibrary.knows(ONE_PIECE)
        openLibrary.answers("/authors/OL2733294A.json", """{"key": "/authors/OL2733294A"}""")

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.Failed
    }

    @Test
    fun `an Open Library that answers an empty body is a failure`() {
        // Given
        openLibrary.answers("/isbn/$ONE_PIECE.json", "")

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.Failed
    }

    @Test
    fun `an Open Library that answers past the timeout is a failure`() {
        // Given
        openLibrary.answersTooLate(ONE_PIECE)

        // When
        val answer = source.lookUp(isbn(ONE_PIECE))

        // Then
        answer shouldBe SourceAnswer.Failed
    }

    private companion object {
        const val ONE_PIECE = "9782723488525"
        val TIMEOUT: Duration = Duration.ofMillis(200)
        val WARM_UP_TIMEOUT: Duration = Duration.ofSeconds(20)
        val server = WireMockServer(options().dynamicPort())
        val openLibrary = OpenLibraryStubs(server)

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            server.start()
            openLibrary.knows(ONE_PIECE)
            OpenLibrarySource(server.baseUrl(), WARM_UP_TIMEOUT).lookUp(isbn(ONE_PIECE))
            server.resetAll()
        }

        fun isbn(text: String): Isbn13 = checkNotNull(Isbn13.of(text))

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            server.stop()
        }
    }
}
