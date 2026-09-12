package fr.amory.libris.infra.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.jayway.jsonpath.JsonPath
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.Source
import fr.amory.libris.domain.SourceAnswer
import fr.amory.libris.domain.SourceAuthor
import fr.amory.libris.domain.SourceEdition
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration

class OpenLibrarySourceTest {
    private val openLibrary = OpenLibrarySource(wireMock.baseUrl(), TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        wireMock.resetAll()
    }

    @Test
    fun `the source names itself`() {
        openLibrary.source shouldBe Source.OPEN_LIBRARY
    }

    @Test
    fun `a known ISBN is what Open Library knows about it`() {
        // Given
        openLibraryKnows(ONE_PIECE)

        // When
        val answer = openLibrary.lookUp(isbn(ONE_PIECE))

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
        openLibraryKnows(ONE_PIECE)

        // When
        openLibrary.lookUp(isbn(ONE_PIECE))

        // Then
        wireMock.allServeEvents.map { it.request.url } shouldContainExactlyInAnyOrder listOf(
            "/isbn/$ONE_PIECE.json",
            "/books/OL33773404M.json",
            "/authors/OL2733294A.json",
            "/authors/OL7476994A.json",
        )
    }

    private companion object {
        const val ONE_PIECE = "9782723488525"
        val TIMEOUT: Duration = Duration.ofMillis(200)
        val WARM_UP_TIMEOUT: Duration = Duration.ofSeconds(20)
        val wireMock = WireMockServer(options().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            wireMock.start()
            openLibraryKnows(ONE_PIECE)
            OpenLibrarySource(wireMock.baseUrl(), WARM_UP_TIMEOUT).lookUp(isbn(ONE_PIECE))
            wireMock.resetAll()
        }

        fun isbn(text: String): Isbn13 = checkNotNull(Isbn13.of(text))

        fun openLibraryKnows(isbn: String) {
            val document = recorded("open-library/books/$isbn.json")
            val key = JsonPath.read<String>(document, "$.key")
            wireMock.stubFor(
                get(urlPathEqualTo("/isbn/$isbn.json")).willReturn(temporaryRedirect("${wireMock.baseUrl()}$key.json")),
            )
            wireMock.stubFor(get(urlPathEqualTo("$key.json")).willReturn(json(document)))
            JsonPath.read<List<String>>(document, "$.authors[*].key").forEach { author ->
                wireMock.stubFor(
                    get(urlPathEqualTo("$author.json")).willReturn(json(recorded("open-library$author.json"))),
                )
            }
        }

        fun json(body: String): ResponseDefinitionBuilder =
            ok().withHeader("Content-Type", "application/json").withBody(body)

        fun recorded(name: String): String =
            checkNotNull(OpenLibrarySourceTest::class.java.getResource("/scenarios/$name")) {
                "no recorded answer $name"
            }.readText()

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            wireMock.stop()
        }
    }
}
