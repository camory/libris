package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
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
    fun `S4 — Unknown ISBN`() {
        // Given
        bnf.stubFor(
            get(urlPathEqualTo("/api/SRU"))
                .withQueryParam("query", containing("9782000000013"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "text/xml;charset=UTF-8")
                        .withBody(recorded("bnf/9782000000013.xml")),
                ),
        )
        openLibrary.stubFor(
            get(urlPathEqualTo("/isbn/9782000000013.json"))
                .willReturn(notFound().withHeader("Content-Type", "text/html; charset=utf-8")),
        )
        // When
        val response = http.get()
            .uri("/api/v1/isbn/9782000000013")
            .headers { it.putAll(READER) }
            .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
            .exchange()
        // Then
        response.expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().jsonPath("$.type").isEqualTo("/problems/not-found")
    }

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
