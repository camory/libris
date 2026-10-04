package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.EditionLookup
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.EditionSource
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Failed
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Known
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.NothingKnown
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.client.RestClientException
import org.springframework.web.client.toEntity
import tools.jackson.databind.JsonNode
import tools.jackson.databind.exc.JsonNodeException
import tools.jackson.databind.node.MissingNode
import java.time.Duration

private const val SEARCH_FIELDS = "key,author_name,edition_key"
private val YEAR = Regex("\\d{4}")

class OpenLibrarySource(baseUrl: String, private val coversUrl: String, timeout: Duration) : EditionLookup, CoverFetch {
  override val source = EditionSource.OPEN_LIBRARY
  override val coverSource = CoverSource.OPEN_LIBRARY
  private val http = sourceRestClient(baseUrl, timeout)

  override fun lookUp(isbn: Isbn): EditionSourceAnswer =
    try {
      answerFor(isbn)
    } catch (ignored: NotFound) {
      NothingKnown
    } catch (ignored: RestClientException) {
      Failed
    } catch (ignored: JsonNodeException) {
      Failed
    }

  override fun fetch(isbn: Isbn): Cover? =
    nullOnFailure { coverOf(isbn) }

  private fun answerFor(isbn: Isbn): EditionSourceAnswer =
    Known(
      previewOf(isbn, document("/isbn/${isbn.digits}.json")),
      CoverCandidate(CoverSource.OPEN_LIBRARY, "$coversUrl/${isbn.digits}-L.jpg?default=false"),
    )

  private fun previewOf(isbn: Isbn, edition: JsonNode): EditionPreview =
    EditionPreview(
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
    )

  private fun contributionsOf(edition: JsonNode, search: JsonNode): Contributions {
    val key = edition["key"]?.asString()?.substringAfterLast("/")
    return Contributions.of(
      search
        .path("docs")
        .values()
        .firstOrNull { work -> work.path("edition_key").values().any { it.asString() == key } }
        ?.path("author_name")
        ?.values()
        ?.mapNotNull { node -> Contribution.of(node.asString(), WRITER) }
        .orEmpty(),
    )
  }

  private fun search(isbn: Isbn): JsonNode =
    http
      .get()
      .uri { uri ->
        uri
          .path("/search.json")
          .queryParam("isbn", isbn.digits)
          .queryParam("fields", SEARCH_FIELDS)
          .build()
      }
      .retrieve()
      .body(JsonNode::class.java) ?: MissingNode.getInstance()

  private fun document(path: String): JsonNode =
    http.get().uri(path).retrieve().body(JsonNode::class.java) ?: MissingNode.getInstance()

  private fun coverOf(isbn: Isbn): Cover? {
    val answer = http
      .get()
      .uri("$coversUrl/${isbn.digits}-L.jpg?default=false")
      .retrieve()
      .toEntity<ByteArray>()
    return answer.body?.let { bytes -> answer.headers.contentType?.let { Cover.of(it.toString(), bytes) } }
  }
}
