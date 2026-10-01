package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.ExternalCoverLookup
import fr.amory.libris.bibliography.domain.lookup.Source.INVENTAIRE
import org.springframework.web.client.RestClientException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.time.Duration

class InventaireCoverLookup(private val baseUrl: String, timeout: Duration) : ExternalCoverLookup {
    private val http = sourceRestClient(baseUrl, timeout)

    override fun lookUp(isbn: Isbn): CoverCandidate? =
        try {
            pictureOf(entities(isbn))?.let { CoverCandidate(INVENTAIRE, "$baseUrl/img/entities/100x600/$it") }
        } catch (ignored: RestClientException) {
            null
        } catch (ignored: JsonNodeException) {
            null
        }

    private fun pictureOf(answer: JsonNode): String? =
        answer.path("entities").values().firstOrNull()?.path("claims")?.path("invp:P2")?.values()?.firstOrNull()
            ?.asString()

    private fun entities(isbn: Isbn): JsonNode = http.get()
        .uri { uri ->
            uri.path("/api/entities")
                .queryParam("action", "by-uris")
                .queryParam("uris", "isbn:${isbn.digits}")
                .build()
        }
        .retrieve()
        .body(JsonNode::class.java) ?: MissingNode.getInstance()
}
