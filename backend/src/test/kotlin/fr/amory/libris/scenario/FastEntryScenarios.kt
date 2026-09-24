package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import io.kotest.matchers.shouldBe
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
    fun `S1 Typed ISBN, found`() {
        // Given
        bnf.knows("9782723488525")
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody().json(
                """
                {
                  "isbn13": "9782723488525",
                  "kind": "MANGA",
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
                  "coverUrl": "$ONE_PIECE_COVER",
                  "copies": []
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
            .jsonPath("$.authors[0].name").isEqualTo("Eiichirō Oda")
            .jsonPath("$.pageCount").isEqualTo(207)
            .jsonPath("$.publicationYear").isEqualTo(2013)
            .jsonPath("$.coverUrl").isEqualTo(ONE_PIECE_COVER)
    }

    @Test
    fun `S5 Merged answer, from Open Library alone`() {
        // Given
        bnf.doesNotKnow("9782380751673")
        openLibrary.knows("9782380751673")
        // When
        val response = ask("9782380751673")
        // Then
        response.expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Space Wars - Chapitre 1")
            .jsonPath("$.authors[*].name").isEqualTo(listOf("Baba", "Stéphane Lapuss'", "Tartuff"))
            .jsonPath("$.publisher").isEqualTo("KENNES EDITIONS")
            .jsonPath("$.coverUrl").isEqualTo("https://covers.openlibrary.org/b/isbn/9782380751673-L.jpg?default=false")
    }

    @Test
    fun `S6 One source down`() {
        // Given
        bnf.fails()
        openLibrary.knows("9782380751673")
        // When
        val response = ask("9782380751673")
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Space Wars - Chapitre 1")
    }

    @Test
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

    @Test
    fun `S7 Every source down, past the timeout`() {
        // Given
        bnf.answersTooLate("9782723488525")
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
        const val ONE_PIECE_COVER =
            "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb43636708p&couverture=1"
        val READER = mapOf(
            "Remote-User" to listOf("juliette"),
            "Remote-Name" to listOf("Juliette"),
            "Remote-Email" to listOf("juliette@amory.fr"),
            "Remote-Groups" to listOf("family"),
        )
    }
}
