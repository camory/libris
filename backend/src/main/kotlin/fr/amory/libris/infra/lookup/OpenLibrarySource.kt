package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.net.http.HttpClient
import java.time.Duration

@Component
class OpenLibrarySource(
    @Value("\${LIBRIS_OPEN_LIBRARY_URL:https://openlibrary.org}") baseUrl: String,
    @Value("\${LIBRIS_SOURCE_TIMEOUT:5s}") timeout: Duration,
) : IsbnSource {
    override val source = Source.OPEN_LIBRARY

    private val http = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory(timeout))
        .build()

    override fun lookUp(isbn: Isbn13): SourceAnswer =
        try {
            answerFor(isbn)
        } catch (ignored: RestClientException) {
            SourceAnswer.Failed
        } catch (ignored: JsonNodeException) {
            SourceAnswer.Failed
        }

    private fun answerFor(isbn: Isbn13): SourceAnswer =
        editionOf(isbn)?.let { answerFrom(isbn, it) } ?: SourceAnswer.NothingKnown

    private fun answerFrom(isbn: Isbn13, edition: JsonNode): SourceAnswer =
        SourceAnswer.Known(
            SourceEdition(
                isbn13 = isbn,
                title = edition.required("title").asString(),
                subtitle = edition["subtitle"]?.asString(),
                authors = authorsOf(edition),
                series = null,
                collection = null,
                publisher = edition.path("publishers").values().firstOrNull()?.asString(),
                publicationYear = yearOf(edition["publish_date"]?.asString()),
                language = null,
                pageCount = edition["number_of_pages"]?.asInt(),
                summary = null,
                coverUrl = "$COVERS/${isbn.digits}-L.jpg",
            ),
        )

    private fun editionOf(isbn: Isbn13): JsonNode? =
        try {
            document("/isbn/${isbn.digits}.json")
        } catch (ignored: HttpClientErrorException.NotFound) {
            null
        }

    private fun authorsOf(edition: JsonNode): List<SourceAuthor> =
        edition.path("authors").values().map { SourceAuthor(nameOf(it.path("key").asString()), WRITER) }

    private fun nameOf(key: String): String = document("$key.json").required("name").asString()

    private fun yearOf(publishDate: String?): Int? = publishDate?.let { YEAR.find(it)?.value?.toInt() }

    private fun document(path: String): JsonNode =
        http.get().uri(path).retrieve().body(JsonNode::class.java) ?: MissingNode.getInstance()

    private companion object {
        const val COVERS = "https://covers.openlibrary.org/b/isbn"
        val YEAR = Regex("\\d{4}")

        fun requestFactory(timeout: Duration): JdkClientHttpRequestFactory {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(timeout)
                .build()
            return JdkClientHttpRequestFactory(client).apply { setReadTimeout(timeout) }
        }
    }
}
