package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.client.RestClientException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.time.Duration

private const val COVERS = "https://covers.openlibrary.org/b/isbn"
private const val SEARCH_FIELDS = "key,author_name,edition_key"
private val YEAR = Regex("\\d{4}")

class OpenLibrarySource(baseUrl: String, timeout: Duration) : IsbnSource {
    private val http = sourceRestClient(baseUrl, timeout)

    override fun lookUp(isbn: Isbn): SourceAnswer =
        try {
            answerFor(isbn)
        } catch (ignored: NotFound) {
            NothingKnown
        } catch (ignored: RestClientException) {
            Failed
        } catch (ignored: JsonNodeException) {
            Failed
        }

    private fun answerFor(isbn: Isbn): SourceAnswer = Known(editionOf(isbn, document("/isbn/${isbn.digits}.json")))

    private fun editionOf(isbn: Isbn, edition: JsonNode): SourceEdition = SourceEdition(
        isbn = isbn,
        kind = null,
        title = edition.required("title").asString(),
        subtitle = edition["subtitle"]?.asString(),
        authors = authorsOf(edition, search(isbn)),
        series = null,
        collection = null,
        publisher = edition.path("publishers").values().firstOrNull()?.asString(),
        publicationYear = YEAR.find(edition["publish_date"]?.asString().orEmpty())?.value?.toIntOrNull(),
        language = null,
        pageCount = edition["number_of_pages"]?.asInt(),
        summary = null,
        coverUrl = "$COVERS/${isbn.digits}-L.jpg?default=false",
    )

    private fun authorsOf(edition: JsonNode, search: JsonNode): List<SourceAuthor> {
        val key = edition["key"]?.asString()?.substringAfterLast("/")
        return search.path("docs").values()
            .firstOrNull { work -> work.path("edition_key").values().any { it.asString() == key } }
            ?.path("author_name")?.values()
            ?.map { SourceAuthor(it.asString(), WRITER) }
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
