package fr.amory.libris.fixture

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.jayway.jsonpath.JsonPath

class OpenLibraryStubs(private val server: WireMockServer) {
    val baseUrl: String get() = server.baseUrl()

    fun knows(isbn: String) {
        val document = recorded("open-library/books/$isbn.json")
        val key = JsonPath.read<String>(document, "$.key")
        server.stubFor(get(urlPathEqualTo("/isbn/$isbn.json")).willReturn(temporaryRedirect("$baseUrl$key.json")))
        server.stubFor(get(urlPathEqualTo("$key.json")).willReturn(json(document)))
        JsonPath.read<List<String>>(document, "$.authors[*].key").forEach { author ->
            server.stubFor(get(urlPathEqualTo("$author.json")).willReturn(json(recorded("open-library$author.json"))))
        }
    }

    fun doesNotKnow(isbn: String) {
        server.stubFor(
            get(urlPathEqualTo("/isbn/$isbn.json"))
                .willReturn(notFound().withHeader("Content-Type", "text/html; charset=utf-8")),
        )
    }

    fun fails() {
        server.stubFor(get(urlPathMatching("/isbn/.*")).willReturn(serverError()))
    }

    fun failsOn(path: String) {
        server.stubFor(get(urlPathEqualTo(path)).willReturn(serverError()))
    }

    fun answers(path: String, body: String) {
        server.stubFor(get(urlPathEqualTo(path)).willReturn(json(body)))
    }

    fun answersTooLate(isbn: String) {
        server.stubFor(get(urlPathEqualTo("/isbn/$isbn.json")).willReturn(ok().withFixedDelay(LATE)))
    }

    private fun json(body: String): ResponseDefinitionBuilder =
        ok().withHeader("Content-Type", "application/json").withBody(body)

    private companion object {
        const val LATE = 2_000
    }
}
