package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAnswer.Failed
import fr.amory.libris.domain.lookup.SourceAnswer.Known
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.time.Duration

class OpenLibrarySource(baseUrl: String, timeout: Duration) : IsbnSource {
    override val source = OPEN_LIBRARY

    private val http = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory(timeout))
        .build()

    override fun lookUp(isbn: Isbn13): SourceAnswer =
        try {
            answerFor(isbn)
        } catch (ignored: RestClientException) {
            Failed
        } catch (ignored: JsonNodeException) {
            Failed
        }

    private fun answerFor(isbn: Isbn13): SourceAnswer =
        editionOf(isbn)?.let { answerFrom(isbn, it) } ?: NothingKnown

    private fun answerFrom(isbn: Isbn13, edition: JsonNode): SourceAnswer =
        Known(
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
                .followRedirects(NORMAL)
                .connectTimeout(timeout)
                .build()
            return JdkClientHttpRequestFactory(client).apply { setReadTimeout(timeout) }
        }
    }
}
