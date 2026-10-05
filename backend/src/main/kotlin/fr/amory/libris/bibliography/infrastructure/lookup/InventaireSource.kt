package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.CoverLookup
import org.springframework.web.client.body
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.MissingNode
import java.time.Duration

class InventaireSource(private val baseUrl: String, timeout: Duration) : CoverLookup, CoverFetch {
  override val coverSource = INVENTAIRE
  private val http = sourceRestClient(baseUrl, timeout)

  override fun lookUpCover(isbn: Isbn): CoverCandidate? =
    nullOnFailure {
      pictureOf(entities(isbn))?.let { CoverCandidate(INVENTAIRE, "$baseUrl/img/entities/100x600/$it") }
    }

  override fun fetchCover(isbn: Isbn): Cover? =
    lookUpCover(isbn)?.let { nullOnFailure { http.pictureAt(it.url) } }

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
}
