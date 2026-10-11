package fr.amory.libris.scenario.dsl

import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.http.MediaType.parseMediaType
import org.springframework.test.web.servlet.client.RestTestClient
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.json.JsonMapper
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

  fun view(reader: Reader, path: String): Answer<Picture> =
    Answer(
      http.get()
        .uri(path)
        .headers { it.putAll(reader.headers) }
        .accept(parseMediaType("image/*"), APPLICATION_PROBLEM_JSON)
        .exchange(),
    ) { pictureOf(it) }

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
