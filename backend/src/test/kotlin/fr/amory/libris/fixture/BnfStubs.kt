package fr.amory.libris.fixture

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo

class BnfStubs(private val server: WireMockServer) {
    val baseUrl: String get() = server.baseUrl()

    fun knows(isbn: String) = answers(isbn, recorded("bnf/$isbn.xml"))

    fun partiallyKnows(isbn: String) = answers(isbn, recorded("bnf/$isbn-without-pages-and-year.xml"))

    fun doesNotKnow(isbn: String) = answers(isbn, recorded("bnf/$isbn.xml"))

    fun fails() {
        server.stubFor(get(urlPathEqualTo(SRU)).willReturn(serverError()))
    }

    private fun answers(isbn: String, envelope: String) {
        server.stubFor(
            get(urlPathEqualTo(SRU))
                .withQueryParam("query", containing(isbn))
                .willReturn(xml(envelope)),
        )
    }

    private fun xml(body: String): ResponseDefinitionBuilder =
        ok().withHeader("Content-Type", "text/xml;charset=UTF-8").withBody(body)

    private companion object {
        const val SRU = "/api/SRU"
    }
}
