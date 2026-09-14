package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import fr.amory.libris.fixture.BnfStubs
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
) {
    private val bnf = BnfStubs(bnfServer)

    @Test
    fun `the application runs over the stubbed sources`() {
        environment.getProperty("LIBRIS_BNF_URL") shouldBe "${bnf.baseUrl}/api/SRU"
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
                  "coverUrl": "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb43636708p&couverture=1",
                  "sources": ["BNF"]
                }
                """,
                JsonCompareMode.STRICT,
            )
    }

    @Test
    fun `S4 Unknown ISBN`() {
        // Given
        bnf.doesNotKnow("9782000000013")
        // When
        val response = ask("9782000000013")
        // Then
        response.expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().jsonPath("$.type").isEqualTo("/problems/not-found")
    }

    @Test
    fun `S7 The source down`() {
        // Given
        bnf.fails()
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isEqualTo(SERVICE_UNAVAILABLE)
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().jsonPath("$.type").isEqualTo("/problems/sources-unavailable")
    }

    @Test
    fun `S7 The source down, past the timeout`() {
        // Given
        bnf.answersTooLate("9782723488525")
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
