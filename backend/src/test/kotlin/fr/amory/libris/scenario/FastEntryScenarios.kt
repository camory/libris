package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.jayway.jsonpath.JsonPath
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class FastEntryScenarios @Autowired constructor(
    private val http: RestTestClient,
    private val environment: Environment,
    @param:Qualifier("bnf") private val bnf: WireMockServer,
    @param:Qualifier("openLibrary") private val openLibrary: WireMockServer,
) {
    @Test
    fun `the application runs over the stubbed sources`() {
        environment.getProperty("LIBRIS_BNF_URL") shouldBe "${bnf.baseUrl()}/api/SRU"
        environment.getProperty("LIBRIS_OPEN_LIBRARY_URL") shouldBe openLibrary.baseUrl()
        environment.getProperty("LIBRIS_SOURCE_TIMEOUT") shouldBe "1s"
    }

    @Test
    @Disabled("S4")
    fun `S4 Unknown ISBN`() {
        // Given
        bnfKnows("9782000000013", "bnf/9782000000013.xml")
        openLibrary.stubFor(
            get(urlPathEqualTo("/isbn/9782000000013.json"))
                .willReturn(notFound().withHeader("Content-Type", "text/html; charset=utf-8")),
        )
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
        bnfKnows("9782723488525", "bnf/9782723488525-without-pages-and-year.xml")
        openLibraryKnows("9782723488525", "OL33773404M")
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
        bnf.stubFor(get(urlPathEqualTo("/api/SRU")).willReturn(serverError()))
        openLibraryKnows("9782723488525", "OL33773404M")
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
        bnfKnows("9782723488525", "bnf/9782723488525.xml")
        openLibrary.stubFor(
            get(urlPathEqualTo("/isbn/9782723488525.json"))
                .willReturn(temporaryRedirect("${openLibrary.baseUrl()}/books/OL33773404M.json").withFixedDelay(2_000)),
        )
        // When
        val response = ask("9782723488525")
        // Then
        response.expectStatus().isOk()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Romance dawn")
            .jsonPath("$.coverUrl").isEqualTo("https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg")
            .jsonPath("$.sources").isEqualTo(listOf("BNF"))
    }

    private fun bnfKnows(isbn: String, record: String) {
        bnf.stubFor(
            get(urlPathEqualTo("/api/SRU"))
                .withQueryParam("query", containing(isbn))
                .willReturn(xml(recorded(record))),
        )
    }

    private fun openLibraryKnows(isbn: String, olid: String) {
        val document = recorded("open-library/books/$olid.json")
        openLibrary.stubFor(
            get(urlPathEqualTo("/isbn/$isbn.json"))
                .willReturn(temporaryRedirect("${openLibrary.baseUrl()}/books/$olid.json")),
        )
        openLibrary.stubFor(get(urlPathEqualTo("/books/$olid.json")).willReturn(json(document)))
        JsonPath.read<List<String>>(document, "$.authors[*].key").forEach { author ->
            openLibrary.stubFor(
                get(urlPathEqualTo("$author.json")).willReturn(json(recorded("open-library$author.json"))),
            )
        }
    }

    private fun ask(isbn: String): RestTestClient.ResponseSpec = http.get()
        .uri("/api/v1/isbn/$isbn")
        .headers { it.putAll(READER) }
        .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
        .exchange()

    private fun xml(body: String): ResponseDefinitionBuilder =
        ok().withHeader("Content-Type", "text/xml;charset=UTF-8").withBody(body)

    private fun json(body: String): ResponseDefinitionBuilder =
        ok().withHeader("Content-Type", "application/json").withBody(body)

    private fun recorded(name: String): String =
        checkNotNull(javaClass.getResource("/scenarios/$name")) { "no recorded answer $name" }.readText()

    private companion object {
        val READER = mapOf(
            "Remote-User" to listOf("juliette"),
            "Remote-Name" to listOf("Juliette"),
            "Remote-Email" to listOf("juliette@amory.fr"),
            "Remote-Groups" to listOf("family"),
        )
    }
}
