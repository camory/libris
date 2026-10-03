package fr.amory.libris.bibliography.fixture

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.jayway.jsonpath.JsonPath

class InventaireStubs(private val server: WireMockServer) {
  val baseUrl: String get() = server.baseUrl()

  fun knows(isbn: String, picture: ByteArray) {
    val entity = recorded("inventaire/$isbn.json")
    entityAnswers(isbn, entity)
    server.stubFor(get(urlPathMatching(picturePath(hashOf(entity)))).willReturn(webp(picture)))
  }

  fun knowsButThePictureNeverComes(isbn: String) {
    val entity = recorded("inventaire/$isbn.json")
    entityAnswers(isbn, entity)
    server.stubFor(get(urlPathMatching(picturePath(hashOf(entity)))).willReturn(ok().withFixedDelay(NEVER)))
  }

  fun knowsWithoutPicture(isbn: String) {
    val entity = JsonPath.parse(recorded("inventaire/$isbn.json")).delete("$..claims['invp:P2']")
    entityAnswers(isbn, entity.jsonString())
  }

  fun knowsWithPictureClaim(isbn: String, claim: Any?) {
    val entity = JsonPath.parse(recorded("inventaire/$isbn.json")).set("$..claims['invp:P2'][0]", claim)
    entityAnswers(isbn, entity.jsonString())
  }

  fun answersTooLate(isbn: String) {
    server.stubFor(
      get(urlPathEqualTo(ENTITIES))
        .withQueryParam("uris", equalTo("isbn:$isbn"))
        .willReturn(json(recorded("inventaire/$isbn.json")).withFixedDelay(LATE)),
    )
  }

  fun doesNotKnow(isbn: String) =
    entityAnswers(isbn, recorded("inventaire/$isbn.json"))

  fun fails() {
    server.stubFor(get(urlPathEqualTo(ENTITIES)).willReturn(serverError()))
  }

  fun pictureRequests(): Int =
    picturePaths().size

  fun picturePaths(): List<String> =
    server.findAll(getRequestedFor(urlPathMatching(picturePath(".*")))).map { it.url }

  private fun entityAnswers(isbn: String, entity: String) {
    server.stubFor(
      get(urlPathEqualTo(ENTITIES))
        .withQueryParam("action", equalTo("by-uris"))
        .withQueryParam("uris", equalTo("isbn:$isbn"))
        .willReturn(json(entity)),
    )
  }

  private fun hashOf(entity: String): String =
    JsonPath.read<List<String>>(entity, "$.entities.*.claims['invp:P2'][0]").single()

  private fun picturePath(hash: String) =
    "/img/entities/([0-9]+x[0-9]+/)?$hash"

  private fun json(body: String): ResponseDefinitionBuilder =
    ok().withHeader("Content-Type", "application/json").withBody(body)

  private fun webp(body: ByteArray): ResponseDefinitionBuilder =
    ok().withHeader("Content-Type", "image/webp").withBody(body)

  private companion object {
    const val ENTITIES = "/api/entities"
    const val NEVER = 30_000
    const val LATE = 2_000
  }
}
