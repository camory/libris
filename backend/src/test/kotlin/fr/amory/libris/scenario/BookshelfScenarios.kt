package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.jayway.jsonpath.JsonPath
import fr.amory.libris.bibliography.fixture.BnfStubs
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class BookshelfScenarios @Autowired constructor(
    private val http: RestTestClient,
    @Qualifier("bnf") bnfServer: WireMockServer,
    @Qualifier("openLibrary") openLibraryServer: WireMockServer,
) {
    private val bnf = BnfStubs(bnfServer)
    private val sources = listOf(bnfServer, openLibraryServer)

    @BeforeEach
    fun forgetEarlierRequests() {
        sources.forEach { it.resetRequests() }
    }

    @Test
    fun `S1 The first visit creates the bookshelf`() {
        // Given
        val lea = reader("lea", "Léa")
        // When
        val response = me(lea)
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.defaultBookshelf.id").isNotEmpty()
            .jsonPath("$.defaultBookshelf.name").isEqualTo("Bibliothèque de Léa")
    }

    @Test
    fun `S2 The ouvrage is added`() {
        // Given
        val juliette = reader("juliette", "Juliette")
        bnf.knows("9782723488525")
        ask(juliette, "9782723488525").expectStatus().isOk()
            .expectBody().jsonPath("$.copies").isEmpty()
        val bookshelf = defaultBookshelfOf(juliette)
        // When
        val response = add(juliette, bookshelf, NEW_ONE_PIECE_1)
        // Then
        response.expectStatus().isCreated()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.bookshelf.id").isEqualTo(bookshelf)
            .jsonPath("$.bookshelf.name").isEqualTo("Bibliothèque de Juliette")
        ask(juliette, "9782723488525").expectStatus().isOk()
            .expectBody()
            .jsonPath("$.kind").isEqualTo("MANGA")
            .jsonPath("$.copies.length()").isEqualTo(1)
            .jsonPath("$.copies[0].bookshelf.name").isEqualTo("Bibliothèque de Juliette")
    }

    @Test
    fun `S3 A known ISBN reaches the existing edition, another reader`() {
        // Given
        val marc = reader("marc", "Marc")
        val nina = reader("nina", "Nina")
        add(marc, defaultBookshelfOf(marc), NEW_ONE_PIECE_2).expectStatus().isCreated()
        // When
        val response = add(nina, defaultBookshelfOf(nina), NEW_ONE_PIECE_2)
        // Then
        response.expectStatus().isCreated()
            .expectBody().jsonPath("$.bookshelf.name").isEqualTo("Bibliothèque de Nina")
        ask(nina, "9782723489898").expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Aux prises avec Baggy et ses hommes")
            .jsonPath("$.copies.length()").isEqualTo(1)
            .jsonPath("$.copies[0].bookshelf.name").isEqualTo("Bibliothèque de Nina")
    }

    @Test
    fun `S3 A known ISBN reaches the existing edition, the same reader again`() {
        // Given
        val paul = reader("paul", "Paul")
        val bookshelf = defaultBookshelfOf(paul)
        val first = idOf(add(paul, bookshelf, NEW_ONE_PIECE_2).expectStatus().isCreated())
        // When
        val response = add(paul, bookshelf, NEW_ONE_PIECE_2)
        // Then
        val second = idOf(response.expectStatus().isCreated())
        second shouldNotBe first
        ask(paul, "9782723489898").expectStatus().isOk()
            .expectBody()
            .jsonPath("$.copies.length()").isEqualTo(2)
            .jsonPath("$.copies[*].bookshelf.name").isEqualTo(listOf("Bibliothèque de Paul", "Bibliothèque de Paul"))
    }

    @Test
    fun `S4 The ouvrage is already in a bookshelf`() {
        // Given
        val rose = reader("rose", "Rose")
        val sam = reader("sam", "Sam")
        val rosesCopy = idOf(add(rose, defaultBookshelfOf(rose), NEW_ONE_PIECE_3).expectStatus().isCreated())
        add(sam, defaultBookshelfOf(sam), NEW_ONE_PIECE_3).expectStatus().isCreated()
        // When
        val response = ask(rose, "9782723489904")
        // Then
        response.expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Piège")
            .jsonPath("$.kind").isEqualTo("MANGA")
            .jsonPath("$.copies.length()").isEqualTo(1)
            .jsonPath("$.copies[0].id").isEqualTo(rosesCopy)
            .jsonPath("$.copies[0].bookshelf.name").isEqualTo("Bibliothèque de Rose")
        noSourceWasAsked()
    }

    @Test
    fun `S4 The ouvrage is already in a bookshelf, none of the reader's`() {
        // Given
        val tom = reader("tom", "Tom")
        val zoe = reader("zoe", "Zoé")
        add(tom, defaultBookshelfOf(tom), NEW_ONE_PIECE_3).expectStatus().isCreated()
        // When
        val response = ask(zoe, "9782723489904")
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Piège")
            .jsonPath("$.copies").isEmpty()
        noSourceWasAsked()
    }

    private fun me(reader: Map<String, List<String>>): RestTestClient.ResponseSpec = http.get()
        .uri("/api/v1/me")
        .headers { it.putAll(reader) }
        .accept(APPLICATION_JSON)
        .exchange()

    private fun ask(reader: Map<String, List<String>>, isbn: String): RestTestClient.ResponseSpec = http.get()
        .uri("/api/v1/isbn/$isbn")
        .headers { it.putAll(reader) }
        .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
        .exchange()

    private fun add(reader: Map<String, List<String>>, bookshelf: String, book: String): RestTestClient.ResponseSpec =
        http.post()
            .uri("/api/v1/bookshelves/$bookshelf/books")
            .headers { it.putAll(reader) }
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
            .body(book)
            .exchange()

    private fun defaultBookshelfOf(reader: Map<String, List<String>>): String =
        JsonPath.read(bodyOf(me(reader).expectStatus().isOk()), "$.defaultBookshelf.id")

    private fun idOf(response: RestTestClient.ResponseSpec): String = JsonPath.read(bodyOf(response), "$.id")

    private fun bodyOf(response: RestTestClient.ResponseSpec): String =
        requireNotNull(response.expectBody(String::class.java).returnResult().responseBody)

    private fun noSourceWasAsked() {
        sources.forEach { it.verify(0, anyRequestedFor(anyUrl())) }
    }

    private fun reader(username: String, name: String) = mapOf(
        "Remote-User" to listOf(username),
        "Remote-Name" to listOf(name),
        "Remote-Email" to listOf("$username@amory.fr"),
        "Remote-Groups" to listOf("family"),
    )

    private companion object {
        val NEW_ONE_PIECE_1 = onePiece(
            isbn = "9782723488525",
            title = "Romance dawn",
            subtitle = "\"à l'aube d'une grande aventure\"",
            volume = 1,
            pages = 203,
        )
        val NEW_ONE_PIECE_2 = onePiece(
            isbn = "9782723489898",
            title = "Aux prises avec Baggy et ses hommes",
            subtitle = "null",
            volume = 2,
            pages = 208,
        )
        val NEW_ONE_PIECE_3 = onePiece(
            isbn = "9782723489904",
            title = "Piège",
            subtitle = "null",
            volume = 3,
            pages = 200,
        )

        fun onePiece(isbn: String, title: String, subtitle: String, volume: Int, pages: Int) =
            """
            {
              "isbn13": "$isbn",
              "kind": "MANGA",
              "title": "$title",
              "subtitle": $subtitle,
              "authors": [
                { "name": "Eiichirō Oda", "role": "WRITER" },
                { "name": "Eiichirō Oda", "role": "ARTIST" }
              ],
              "series": { "name": "One piece", "volumeNumber": $volume },
              "collection": "Shonen manga",
              "publisher": "Glénat",
              "publicationYear": 2013,
              "language": "fr",
              "pageCount": $pages,
              "summary": null,
              "coverUrl": "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg"
            }
            """.trimIndent()
    }
}
