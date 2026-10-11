package fr.amory.libris.scenario.dsl

import com.jayway.jsonpath.JsonPath
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.web.servlet.client.RestTestClient
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
import java.util.Optional
import java.util.UUID

class Libris(private val http: RestTestClient) {
  fun reader(username: String, displayName: String) =
    Reader(this, username, displayName)

  fun knownReader(username: String, displayName: String) =
    reader(username, displayName).also { it.id }

  fun unknownReader() =
    Reader(this, "inconnu", "Inconnu", UUID.randomUUID().toString())

  fun <T> get(reader: Reader, path: String, read: (String) -> T): Answer<T> =
    Answer(
      http.get()
        .uri(path)
        .headers { it.putAll(reader.headers) }
        .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
        .exchange(),
    ) { read(textOf(it)) }

  fun <T> post(reader: Reader, path: String, body: Any, read: (String) -> T): Answer<T> =
    Answer(
      http.post()
        .uri(path)
        .headers { it.putAll(reader.headers) }
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
        .body(JSON.writeValueAsString(body))
        .exchange(),
    ) { read(textOf(it)) }

  fun put(reader: Reader, path: String, body: Any): Answer<Unit> =
    Answer(
      http.put()
        .uri(path)
        .headers { it.putAll(reader.headers) }
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
        .body(JSON.writeValueAsString(body))
        .exchange(),
    ) {}

  fun me(reader: Reader): RestTestClient.ResponseSpec =
    http.get()
      .uri("/api/v1/me")
      .headers { it.putAll(reader.headers) }
      .accept(APPLICATION_JSON)
      .exchange()

  fun defaultBookshelfOf(reader: Reader): String =
    JsonPath.read(bodyOf(me(reader).expectStatus().isOk()), "$.defaultBookshelf.id")

  fun ask(reader: Reader, isbn: String): RestTestClient.ResponseSpec =
    http.get()
      .uri("/api/v1/isbn/$isbn")
      .headers { it.putAll(reader.headers) }
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .exchange()

  fun add(reader: Reader, bookshelf: String, book: String): RestTestClient.ResponseSpec =
    http.post()
      .uri("/api/v1/bookshelves/$bookshelf/books")
      .headers { it.putAll(reader.headers) }
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .body(book)
      .exchange()

  fun catalogue(reader: Reader, after: String? = null): RestTestClient.ResponseSpec =
    http.get()
      .uri { it.path("/api/v1/books").queryParamIfPresent("after", Optional.ofNullable(after)).build() }
      .headers { it.putAll(reader.headers) }
      .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
      .exchange()
}

class Reader(val libris: Libris, username: String, val displayName: String, private val unknownId: String? = null) {
  val headers = mapOf(
    "Remote-User" to listOf(username),
    "Remote-Name" to listOf(displayName),
    "Remote-Email" to listOf("$username@amory.fr"),
    "Remote-Groups" to listOf("family"),
  )

  val id: String
    get() = unknownId ?: me().id

  val defaultBookshelf: Bookshelf
    get() = me().defaultBookshelf

  fun visits(): Answer<Me> =
    libris.get(this, "/api/v1/me") { read<Me>(it) }

  private fun me(): Me =
    visits().value
}

data class Me(val id: String, val defaultBookshelf: Bookshelf)

@PublishedApi
internal val JSON: JsonMapper = JsonMapper.builder().build()

inline fun <reified T> read(body: String): T =
  JSON.readValue(body, object : TypeReference<T>() {})

fun bodyOf(response: RestTestClient.ResponseSpec): String =
  requireNotNull(response.expectBody(String::class.java).returnResult().responseBody)
