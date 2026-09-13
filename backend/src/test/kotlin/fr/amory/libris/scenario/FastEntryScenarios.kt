package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import fr.amory.libris.fixture.BnfStubs
import fr.amory.libris.fixture.OpenLibraryStubs
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class FastEntryScenarios @Autowired constructor(
    private val http: RestTestClient,
    private val environment: Environment,
    @Qualifier("bnf") bnfServer: WireMockServer,
    @Qualifier("openLibrary") openLibraryServer: WireMockServer,
) {
    private val bnf = BnfStubs(bnfServer)
    private val openLibrary = OpenLibraryStubs(openLibraryServer)

    @Test
    fun `the application runs over the stubbed sources`() {
        environment.getProperty("LIBRIS_BNF_URL") shouldBe "${bnf.baseUrl}/api/SRU"
        environment.getProperty("LIBRIS_OPEN_LIBRARY_URL") shouldBe openLibrary.baseUrl
        environment.getProperty("LIBRIS_SOURCE_TIMEOUT") shouldBe "1s"
    }

    @Test
    @Disabled("S1")
    fun `S1 Typed ISBN, found`() {
        // Given
        bnf.knows("9782723488525")
        openLibrary.knows("9782723488525")
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody().json(
                """
                {
                  "isbn13": "9782723488525",
                  "title": "Romance dawn",
                  "subtitle": "à l'aube d'une grande aventure",
                  "authors": [{ "name": "Eiichirō Oda", "role": "WRITER" }],
                  "series": { "name": "One piece", "volumeNumber": 1 },
                  "collection": "Shonen manga",
                  "publisher": "Glénat",
                  "publicationYear": 2013,
                  "language": "fr",
                  "pageCount": 203,
                  "summary": null,
                  "coverUrl": "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
                  "sources": ["BNF", "OPEN_LIBRARY"]
                }
                """,
                JsonCompareMode.STRICT,
            )
    }

    @Test
    fun `S4 Unknown ISBN`() {
        // Given
        bnf.doesNotKnow("9782000000013")
        openLibrary.doesNotKnow("9782000000013")
        // When
        val response = ask("9782000000013")
        // Then
        response.expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().jsonPath("$.type").isEqualTo("/problems/not-found")
    }

    @Test
    @Disabled("S5")
    fun `S5 Merged answer`() {
        // Given
        bnf.partiallyKnows("9782723488525")
        openLibrary.knows("9782723488525")
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Romance dawn")
            .jsonPath("$.pageCount").isEqualTo(207)
            .jsonPath("$.publicationYear").isEqualTo(2013)
            .jsonPath("$.coverUrl").isEqualTo("https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg")
            .jsonPath("$.sources").isEqualTo(listOf("BNF", "OPEN_LIBRARY"))
    }

    @Test
    @Disabled("S6")
    fun `S6 One source down`() {
        // Given
        bnf.fails()
        openLibrary.knows("9782723488525")
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("One Piece - Édition originale Tome 01")
            .jsonPath("$.coverUrl").isEqualTo("https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg")
            .jsonPath("$.sources").isEqualTo(listOf("OPEN_LIBRARY"))
    }

    @Test
    @Disabled("S6")
    fun `S6 One source down, past the timeout`() {
        // Given
        bnf.knows("9782723488525")
        openLibrary.answersTooLate("9782723488525")
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Romance dawn")
            .jsonPath("$.coverUrl").isEqualTo("https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg")
            .jsonPath("$.sources").isEqualTo(listOf("BNF"))
    }

    @Test
    fun `S7 Every source down`() {
        // Given
        bnf.fails()
        openLibrary.fails()
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isEqualTo(SERVICE_UNAVAILABLE)
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().jsonPath("$.type").isEqualTo("/problems/sources-unavailable")
    }

    private fun ask(isbn: String): RestTestClient.ResponseSpec = http.get()
        .uri("/api/v1/isbn/$isbn")
        .headers { it.putAll(READER) }
        .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
        .exchange()

    private companion object {
        val READER = mapOf(
            "Remote-User" to listOf("juliette"),
            "Remote-Name" to listOf("Juliette"),
            "Remote-Email" to listOf("juliette@amory.fr"),
            "Remote-Groups" to listOf("family"),
        )
    }
}
