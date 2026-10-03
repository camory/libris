package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.CoverLookup
import org.springframework.http.InvalidMediaTypeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import org.springframework.web.client.toEntity
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.time.Duration

class InventaireCoverLookup(private val baseUrl: String, timeout: Duration) : CoverLookup, CoverFetch {
  private val http = sourceRestClient(baseUrl, timeout)

  override fun lookUp(isbn: Isbn): CoverCandidate? =
    try {
      pictureOf(entities(isbn))?.let { CoverCandidate(INVENTAIRE, "$baseUrl/img/entities/100x600/$it") }
    } catch (_: RestClientException) {
      null
    } catch (_: JsonNodeException) {
      null
    } catch (_: InvalidMediaTypeException) {
      null
    }

  override fun fetch(isbn: Isbn): Cover? =
    lookUp(isbn)?.let { pictureAt(it.url) }

  private fun pictureOf(answer: JsonNode): String? =
    answer
      .path("entities")
      .values()
      .firstOrNull()
      ?.path("claims")
      ?.path("invp:P2")
      ?.values()
      ?.firstOrNull()
      ?.takeIf { it.isString }
      ?.asString()
      ?.takeIf { it.isNotBlank() }

  private fun entities(isbn: Isbn): JsonNode =
    http
      .get()
      .uri { uri ->
        uri
          .path("/api/entities")
          .queryParam("action", "by-uris")
          .queryParam("uris", "isbn:${isbn.digits}")
          .build()
      }
      .retrieve()
      .body<JsonNode>() ?: MissingNode.getInstance()

  private fun pictureAt(address: String): Cover? =
    try {
      val answer = http
        .get()
        .uri(address)
        .retrieve()
        .toEntity<ByteArray>()
      answer.body?.let { bytes -> answer.headers.contentType?.let { Cover.of(it.toString(), bytes) } }
    } catch (_: RestClientException) {
      null
    } catch (_: IllegalArgumentException) {
      null
    }
}
