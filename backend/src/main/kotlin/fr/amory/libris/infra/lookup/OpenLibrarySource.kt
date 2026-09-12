package fr.amory.libris.infra.lookup

import tools.jackson.databind.JsonNode
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.IsbnSource
import fr.amory.libris.domain.Source
import fr.amory.libris.domain.SourceAnswer
import fr.amory.libris.domain.SourceAuthor
import fr.amory.libris.domain.SourceEdition
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
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

    override fun lookUp(isbn: Isbn13): SourceAnswer {
        val edition = document("/isbn/${isbn.digits}.json")
        return SourceAnswer.Known(
            SourceEdition(
                isbn13 = isbn,
                title = edition["title"].asString(),
                subtitle = edition["subtitle"]?.asString(),
                authors = authorsOf(edition),
                series = null,
                collection = null,
                publisher = edition["publishers"]?.firstOrNull()?.asString(),
                publicationYear = yearOf(edition["publish_date"]?.asString()),
                language = null,
                pageCount = edition["number_of_pages"]?.asInt(),
                summary = null,
                coverUrl = "$COVERS/${isbn.digits}-L.jpg",
            ),
        )
    }

    private fun authorsOf(edition: JsonNode): List<SourceAuthor> =
        edition.path("authors").values().map { SourceAuthor(nameOf(it.path("key").asString()), WRITER) }

    private fun nameOf(key: String): String = document("$key.json")["name"].asString()

    private fun yearOf(publishDate: String?): Int? = publishDate?.let { YEAR.find(it)?.value?.toInt() }

    private fun document(path: String): JsonNode =
        checkNotNull(http.get().uri(path).retrieve().body(JsonNode::class.java))

    private companion object {
        const val COVERS = "https://covers.openlibrary.org/b/isbn"
        val YEAR = Regex("\\d{4}")

        fun requestFactory(timeout: Duration): JdkClientHttpRequestFactory {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(timeout)
                .build()
            return JdkClientHttpRequestFactory(client).apply { setReadTimeout(timeout) }
        }
    }
}
