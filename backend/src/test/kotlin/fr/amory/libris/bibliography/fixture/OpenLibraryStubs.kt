package fr.amory.libris.bibliography.fixture

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
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
    val edition = recorded("open-library/books/$isbn.json")
    val key = JsonPath.read<String>(edition, "$.key")
    server.stubFor(get(urlPathEqualTo("/isbn/$isbn.json")).willReturn(temporaryRedirect("$baseUrl$key.json")))
    server.stubFor(get(urlPathEqualTo("$key.json")).willReturn(json(edition)))
    server.stubFor(
      get(urlPathEqualTo("/search.json")).withQueryParam("isbn", equalTo(isbn))
        .willReturn(json(recorded("open-library/search/$isbn.json"))),
    )
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

  fun hasCover(isbn: String, picture: ByteArray) {
    server.stubFor(
      get(urlPathEqualTo(cover(isbn)))
        .willReturn(jpeg(picture)),
    )
  }

  fun hasCoverAfter(isbn: String, picture: ByteArray, delayMillis: Int) {
    server.stubFor(
      get(urlPathEqualTo(cover(isbn)))
        .willReturn(jpeg(picture).withFixedDelay(delayMillis)),
    )
  }

  fun hasNoCover(isbn: String) {
    server.stubFor(get(urlPathEqualTo(cover(isbn))).willReturn(notFound()))
  }

  fun coverFails(isbn: String) {
    server.stubFor(get(urlPathEqualTo(cover(isbn))).willReturn(serverError()))
  }

  fun coverAnswersTooLate(isbn: String) {
    server.stubFor(get(urlPathEqualTo(cover(isbn))).willReturn(ok().withFixedDelay(LATE)))
  }

  fun coverIsNotAPicture(isbn: String) {
    server.stubFor(
      get(urlPathEqualTo(cover(isbn)))
        .willReturn(ok().withHeader("Content-Type", "text/html; charset=utf-8").withBody("<html>")),
    )
  }

  fun coverRequests(isbn: String): Int =
    server.findAll(getRequestedFor(urlPathEqualTo(cover(isbn)))).size

  fun coverRequestTimes(isbn: String): List<java.util.Date> =
    server.findAll(getRequestedFor(urlPathEqualTo(cover(isbn)))).map { it.loggedDate }

  private fun cover(isbn: String) =
    "/b/isbn/$isbn-L.jpg"

  private fun jpeg(body: ByteArray): ResponseDefinitionBuilder =
    ok().withHeader("Content-Type", "image/jpeg").withBody(body)

  private fun json(body: String): ResponseDefinitionBuilder =
    ok().withHeader("Content-Type", "application/json").withBody(body)

  private companion object {
    const val LATE = 2_000
  }
}
