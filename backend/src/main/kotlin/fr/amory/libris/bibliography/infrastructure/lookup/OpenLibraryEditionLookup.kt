package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.domain.lookup.Source.OPEN_LIBRARY
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.client.RestClientException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.time.Duration

private const val COVERS = "https://covers.openlibrary.org/b/isbn"
private const val SEARCH_FIELDS = "key,author_name,edition_key"
private val YEAR = Regex("\\d{4}")

class OpenLibraryEditionLookup(baseUrl: String, timeout: Duration) : ExternalEditionLookup {
    override val source = OPEN_LIBRARY
    private val http = sourceRestClient(baseUrl, timeout)

    override fun lookUp(isbn: Isbn): ExternalLookupResult =
        try {
            answerFor(isbn)
        } catch (ignored: NotFound) {
            NothingKnown
        } catch (ignored: RestClientException) {
            Failed
        } catch (ignored: JsonNodeException) {
            Failed
        }

    private fun answerFor(isbn: Isbn): ExternalLookupResult =
        Known(previewOf(isbn, document("/isbn/${isbn.digits}.json")))

    private fun previewOf(isbn: Isbn, edition: JsonNode): EditionPreview = EditionPreview(
        isbn = isbn,
        kind = BOOK,
        title = edition.required("title").asString(),
        subtitle = edition["subtitle"]?.asString(),
        contributions = contributionsOf(edition, search(isbn)),
        series = null,
        collection = null,
        publisher = edition.path("publishers").values().firstOrNull()?.asString(),
        publicationYear = YEAR.find(edition["publish_date"]?.asString().orEmpty())?.value?.toIntOrNull(),
        language = null,
        pageCount = edition["number_of_pages"]?.asInt(),
        summary = null,
        coverUrl = "$COVERS/${isbn.digits}-L.jpg?default=false",
    )

    private fun contributionsOf(edition: JsonNode, search: JsonNode): List<Contribution> {
        val key = edition["key"]?.asString()?.substringAfterLast("/")
        return search.path("docs").values()
            .firstOrNull { work -> work.path("edition_key").values().any { it.asString() == key } }
            ?.path("author_name")?.values()
            ?.mapNotNull { node ->
                node.asString()
                    .takeIf { it.isNotBlank() }
                    ?.let { Contribution(it, WRITER) }
            }
            .orEmpty()
    }

    private fun search(isbn: Isbn): JsonNode = http.get()
        .uri { uri ->
            uri.path("/search.json")
                .queryParam("isbn", isbn.digits)
                .queryParam("fields", SEARCH_FIELDS)
                .build()
        }
        .retrieve()
        .body(JsonNode::class.java) ?: MissingNode.getInstance()

    private fun document(path: String): JsonNode =
        http.get().uri(path).retrieve().body(JsonNode::class.java) ?: MissingNode.getInstance()
}
